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
    def tmpDir = File.createTempDir()

    def setup() {
        configDiscovery.getAppDataDir() >> "/tmp/app_0987654321"
        configDiscovery.getNextcloudDir() >> tmpDir
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

        def sut = new NextcloudDB(configDiscovery, mimeTypeRepository, filecacheRepository, groupFolderRepository)

        when:
        sut.appendChildren(parentNode)

        then: "appends only items with known media types"
        parentNode.items.any()
        parentNode.items.find { it.name == 'foo.jpg' }.format.mime == JPEG.mime
        parentNode.items.find { it.name == 'bar.jpeg' }.format.mime == JPEG.mime
        parentNode.items.find { it.name == 'baz.mp3' }.format.mime == MP3.mime
        parentNode.items.find { it.name == 'blah.mp4' }.format.mime == MP4.mime
        parentNode.items.find { it.name == 'readme.md' } == null

        then: "appends all subnodes without their children"
        parentNode.nodes.size() == 1
        with(parentNode.nodes[0]) {
            assert it.name == 'documents'
            assert it.items == []
            assert it.nodes == []
        }
    }

    private static def aFilecache(int id, String path, int parent, int mimeType) {
        def name = new File(path).getName()
        return new Filecache(id, thumbStorageId, path, parent, name, mimeType, 0L, 0L, 0L)
    }
}
