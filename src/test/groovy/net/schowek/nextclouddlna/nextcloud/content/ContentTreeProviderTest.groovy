package net.schowek.nextclouddlna.nextcloud.content

import net.schowek.nextclouddlna.nextcloud.db.NextcloudDB
import spock.lang.Specification
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import static java.time.Instant.now
import static java.time.Instant.ofEpochSecond
import static java.time.temporal.ChronoUnit.DAYS

class ContentTreeProviderTest extends Specification {
    def nextcloudDB

    void setup() {
        nextcloudDB = Mock(NextcloudDB)
        nextcloudDB.mainNodes() >> []
        nextcloudDB.groupFolders() >> []
        nextcloudDB.processThumbnails(_ as Consumer<ContentItem>) >> null
    }

    def "should rebuild tree if it's outdated"() {
        given:
        def clock = Clock.fixed(now, ZoneId.systemDefault())

        def sut = new ContentTreeProvider(nextcloudDB, clock, ["/**"])
        sut.lastBuildTime = lastBuildTime.epochSecond
        nextcloudDB.maxMtime() >> maxMtime.epochSecond

        when:
        def rebuild = sut.rebuildTree(force)

        then:
        rebuild == expectedResult

        where:
        force | now                  | lastBuildTime     | maxMtime             || expectedResult
        true  | now()                | ofEpochSecond(0L) | now().minus(1, DAYS) || true
        true  | now()                | ofEpochSecond(0L) | now().plus(1, DAYS)  || true
        false | now()                | ofEpochSecond(0L) | now().minus(1, DAYS) || true
        false | now()                | ofEpochSecond(0L) | now().plus(1, DAYS)  || true
        false | now()                | now()             | now().minus(1, DAYS) || false
        false | now()                | now()             | now().plus(1, DAYS)  || true
        false | now().plus(1, DAYS)  | now()             | now().minus(1, DAYS) || true
        false | now().minus(1, DAYS) | now()             | now().minus(1, DAYS) || false
    }

    def "shouldAddNode matches top-level folder against scanned patterns"() {
        given:
        def sut = new ContentTreeProvider(nextcloudDB, Clock.fixed(Instant.now(), ZoneId.systemDefault()), scannedFolders)
        def tree = new ContentTree()
        // parentId=999 is not in tree → getFullName returns "/john"
        def node = new ContentNode(1, 999, "john")

        expect:
        sut.shouldAddNode(node, tree) == expected

        where:
        scannedFolders        || expected
        ["/**"]               || true
        ["/john"]             || true
        ["/jane"]             || false
        ["/john", "/jane"]    || true
        ["/**/john"]          || false  // glob: ** requires at least one segment, so /john does not match /**/john
    }

    def "shouldAddNode matches nested folder against scanned patterns"() {
        given:
        def sut = new ContentTreeProvider(nextcloudDB, Clock.fixed(Instant.now(), ZoneId.systemDefault()), scannedFolders)
        def tree = new ContentTree()
        def john = new ContentNode(1, 999, "john")
        tree.addNode(john)                          // john IS in tree → full name = "/john/music"
        def music = new ContentNode(2, 1, "music")

        expect:
        sut.shouldAddNode(music, tree) == expected

        where:
        scannedFolders        || expected
        ["/**"]               || true
        ["/**/music"]         || true
        ["/john/music"]       || true
        ["/jane/music"]       || false
        ["/john"]             || false              // exact match, does not cover subfolders
    }

    def "rebuildTree is synchronized - second call waits for first to finish"() {
        given: "a fresh mock where mainNodes() blocks on a latch after the first (constructor) call"
        def callCount = new AtomicInteger(0)
        def holdLatch = new CountDownLatch(1)
        def enteredLatch = new CountDownLatch(1)

        def db = Mock(NextcloudDB)
        db.groupFolders() >> []
        db.processThumbnails(_) >> null
        db.maxMtime() >> Instant.now().plusSeconds(1000).epochSecond
        db.mainNodes() >> {
            if (callCount.incrementAndGet() > 1) {
                enteredLatch.countDown()   // signal: T1 is now inside rebuildTree
                holdLatch.await(5, TimeUnit.SECONDS)
            }
            return []
        }

        def clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        def sut = new ContentTreeProvider(db, clock, ["/**"])

        when: "T1 enters rebuildTree and is held inside mainNodes()"
        def t2Done = new AtomicBoolean(false)
        def t1 = Thread.start { sut.rebuildTree(true) }
        enteredLatch.await(5, TimeUnit.SECONDS)

        and: "T2 tries to call rebuildTree concurrently"
        def t2 = Thread.start {
            sut.rebuildTree(true)
            t2Done.set(true)
        }
        Thread.sleep(200)

        then: "T2 is blocked — @Synchronized prevents concurrent execution"
        !t2Done.get()

        cleanup:
        holdLatch.countDown()
        t1.join(5000)
        t2.join(5000)
    }
}
