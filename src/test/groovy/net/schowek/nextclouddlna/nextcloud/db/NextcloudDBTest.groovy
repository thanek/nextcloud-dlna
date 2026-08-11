package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import spock.lang.Specification

import static net.schowek.nextclouddlna.nextcloud.content.MediaFormat.*

class NextcloudDBTest extends Specification {
    static final def thumbStorageId = 2
    static final def TEXT_MD = 1
    static final def VIDEO_MP4 = 2
    static final def IMAGE_JPG = 3
    static final def DIRECTORY = 4
    static final def AUDIO_MP3 = 5
    static final def APP_PDF = 6
    def configDiscovery = Mock(NextcloudConfigDiscovery)
    def mimeTypeRepository = Mock(MimetypeRepository)
    def filecacheRepository = Mock(FilecacheRepository)
    def groupFolderRepository = Mock(GroupFolderRepository)
    def thumbnailProcessor = Mock(ThumbnailProcessor)
    def tmpDir = File.createTempDir()

    def storageUserMapper = new DefaultStorageUserMapper()
    def mimetypeResolver
    def pathBuilder
    def contentItemFactory
    def sut

    def setup() {
        configDiscovery.getAppDataDir() >> "/tmp/app_0987654321"
        configDiscovery.getNextcloudDir() >> tmpDir
        configDiscovery.supportsGroupFolders >> false
        filecacheRepository.findFirstByPath(configDiscovery.appDataDir)
                >> new Filecache(999, thumbStorageId, "thumbs", 0, "thumbs", DIRECTORY, 123L, 0L, 0L)
        mimeTypeRepository.findAll() >> [
                new Mimetype(DIRECTORY, 'httpd/unix-directory'),
                new Mimetype(TEXT_MD, 'text/markdown'),
                new Mimetype(AUDIO_MP3, 'audio/mpeg'),
                new Mimetype(VIDEO_MP4, 'video/mp4'),
                new Mimetype(IMAGE_JPG, 'image/jpeg'),
                new Mimetype(APP_PDF, 'application/pdf')
        ]

        mimetypeResolver = new DefaultMimetypeResolver(mimeTypeRepository)
        pathBuilder = new DefaultPathBuilder(configDiscovery, storageUserMapper)
        contentItemFactory = new DefaultContentItemFactory(mimetypeResolver)
        sut = new NextcloudDB(
                configDiscovery, filecacheRepository, groupFolderRepository,
                mimetypeResolver, storageUserMapper, pathBuilder,
                contentItemFactory, thumbnailProcessor
        )
    }

    def cleanup() {
        tmpDir.delete()
    }

    def "should append children to the parent node"() {
        given:
        def parentId = 123
        def parentNode = new ContentNode(parentId, 0, "stuff")
        filecacheRepository.findByParent(parentId) >> [
                aFilecache(1, "/stuff/foo.jpg", parentId, IMAGE_JPG),
                aFilecache(2, "/stuff/readme.md", parentId, TEXT_MD),
                aFilecache(4, "/stuff/baz.mp3", parentId, AUDIO_MP3),
                aFilecache(3, "/stuff/bar.jpeg", parentId, IMAGE_JPG),
                aFilecache(5, "/stuff/blah.mp4", parentId, VIDEO_MP4),
                aFilecache(6, "/stuff/documents", parentId, DIRECTORY),
                aFilecache(7, "/stuff/documents/resume.pdf", 6, APP_PDF)
        ]

        when:
        sut.appendChildren(parentNode)

        then: "appends only items with known media types"
        parentNode.items.any()
        parentNode.items.find { it.name == 'foo.jpg' }.format.mime == JPEG.mime
        parentNode.items.find { it.name == 'bar.jpeg' }.format.mime == JPEG.mime
        parentNode.items.find { it.name == 'baz.mp3' }.format.mime == MP3.mime
        parentNode.items.find { it.name == 'blah.mp4' }.format.mime == MP4.mime
        parentNode.items.find { it.name == 'readme.md' } == null

        then: "item paths are resolved against the nextcloud data dir"
        parentNode.items.every { it.path.startsWith(tmpDir.absolutePath) }

        then: "appends all subnodes without their children"
        parentNode.nodes.size() == 1
        with(parentNode.nodes[0]) {
            assert it.name == 'documents'
            assert it.items == []
            assert it.nodes == []
        }
    }

    def "should fail with descriptive error when FOLDER_MIME_TYPE is missing from database"() {
        given: "fresh mock so setup() stub does not interfere"
        def badMimetypeRepo = Mock(MimetypeRepository)
        badMimetypeRepo.findAll() >> [
                new Mimetype(1, 'text/plain'),
                new Mimetype(2, 'video/mp4')
                // httpd/unix-directory intentionally missing
        ]

        when:
        new DefaultMimetypeResolver(badMimetypeRepo)

        then:
        def e = thrown(IllegalStateException)
        e.message.contains('httpd/unix-directory')
        e.message.contains('Nextcloud database')
    }

    def "should skip item with unknown mimetype and not throw"() {
        given:
        def parentNode = new ContentNode(1, 0, "stuff")
        filecacheRepository.findByParent(1) >> [
                aFilecache(10, "/stuff/unknown.xyz", 1, 99)  // mimetype id 99 not in map
        ]

        when:
        sut.appendChildren(parentNode)

        then: "item is silently skipped — no exception, no items added"
        parentNode.items.isEmpty()
        parentNode.nodes.isEmpty()
    }

    def "mainNodes registers the storage-to-user mapping used when building paths"() {
        given:
        def filecache = aFilecache(20, "files/movie.mp4", 0, VIDEO_MP4)
        filecacheRepository.mainNodes() >> [[filecache, new Mount(1, thumbStorageId, 0, "alice", "LocalHomeMountProvider")] as Object[]]

        when:
        def nodes = sut.mainNodes()

        then: "node is named after the mount owner"
        nodes.size() == 1
        nodes[0].name == "alice"

        and: "subsequent paths for that storage include the user name"
        pathBuilder.build(filecache) == "${tmpDir.absolutePath}/alice/files/movie.mp4"
    }

    private static def aFilecache(int id, String path, int parent, int mimeType) {
        def name = new File(path).getName()
        return new Filecache(id, thumbStorageId, path, parent, name, mimeType, 0L, 0L, 0L)
    }
}
