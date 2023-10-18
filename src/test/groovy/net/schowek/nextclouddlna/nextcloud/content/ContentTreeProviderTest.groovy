package net.schowek.nextclouddlna.nextcloud.content

import net.schowek.nextclouddlna.nextcloud.NextcloudDB
import spock.lang.Specification
import java.time.Clock
import java.time.ZoneId
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

        def sut = new ContentTreeProvider(nextcloudDB, clock)
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
}
