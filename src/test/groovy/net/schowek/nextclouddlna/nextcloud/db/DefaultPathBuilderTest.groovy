package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import spock.lang.Specification

class DefaultPathBuilderTest extends Specification {
    static final def USER_STORAGE = 2
    static final def GROUP_STORAGE = 7

    def nextcloudDir = new File("/var/www/nextcloud/data")
    def configDiscovery = Mock(NextcloudConfigDiscovery)
    def storageUserMapper = new DefaultStorageUserMapper()
    def sut = new DefaultPathBuilder(configDiscovery, storageUserMapper)

    def setup() {
        configDiscovery.getNextcloudDir() >> nextcloudDir
    }

    def "prefixes the path with the user name for a mapped storage"() {
        given:
        storageUserMapper.map(USER_STORAGE, "alice")

        when:
        def path = sut.build(aFilecache(USER_STORAGE, "files/holiday/beach.jpg"))

        then:
        path == "${nextcloudDir.absolutePath}/alice/files/holiday/beach.jpg"
    }

    def "falls back to the bare data dir for an unmapped storage"() {
        expect: "group folders have no owner, so no user segment is inserted"
        sut.build(aFilecache(GROUP_STORAGE, "__groupfolders/3/movie.mp4")) ==
                "${nextcloudDir.absolutePath}/__groupfolders/3/movie.mp4"
    }

    def "maps each storage to its own user"() {
        given:
        storageUserMapper.map(USER_STORAGE, "alice")
        storageUserMapper.map(GROUP_STORAGE, "bob")

        expect:
        sut.build(aFilecache(USER_STORAGE, "files/a.mp3")) == "${nextcloudDir.absolutePath}/alice/files/a.mp3"
        sut.build(aFilecache(GROUP_STORAGE, "files/b.mp3")) == "${nextcloudDir.absolutePath}/bob/files/b.mp3"
    }

    private static def aFilecache(int storage, String path) {
        new Filecache(1, storage, path, 0, new File(path).getName(), 5, 0L, 0L, 0L)
    }
}
