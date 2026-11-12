package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import net.schowek.nextclouddlna.nextcloud.content.Thumbnail
import spock.lang.Specification

import static net.schowek.nextclouddlna.nextcloud.content.MediaFormat.JPEG
import static net.schowek.nextclouddlna.nextcloud.content.MediaFormat.PNG

class FilecacheThumbnailProcessorTest extends Specification {
    static final int APPDATA_STORAGE = 2
    static final int DIRECTORY = 1
    static final int IMAGE_JPEG = 2
    static final int IMAGE_PNG = 3
    static final int TEXT_PLAIN = 4
    static final String APPDATA = "appdata_oc123"

    def nextcloudDir = new File("/var/www/nextcloud/data")
    def configDiscovery = Mock(NextcloudConfigDiscovery)
    def filecacheRepository = Mock(FilecacheRepository)
    def mimetypeRepository = Mock(MimetypeRepository)
    def sut

    def setup() {
        configDiscovery.getAppDataDir() >> APPDATA
        configDiscovery.getNextcloudDir() >> nextcloudDir
        filecacheRepository.findFirstByPath(APPDATA) >> new Filecache(1, APPDATA_STORAGE, APPDATA, 0, APPDATA, DIRECTORY, 0L, 0L, 0L)
        mimetypeRepository.findAll() >> [
                new Mimetype(DIRECTORY, 'httpd/unix-directory'),
                new Mimetype(IMAGE_JPEG, 'image/jpeg'),
                new Mimetype(IMAGE_PNG, 'image/png'),
                new Mimetype(TEXT_PLAIN, 'text/plain')
        ]
        def mimetypeResolver = new DefaultMimetypeResolver(mimetypeRepository)
        def pathBuilder = new DefaultPathBuilder(configDiscovery, new DefaultStorageUserMapper())
        sut = new FilecacheThumbnailProcessor(configDiscovery, filecacheRepository, mimetypeResolver, pathBuilder)
    }

    def "reads item id and dimensions from preview files stored in filecache"() {
        given:
        filecacheRepository.findThumbnails("$APPDATA/preview/%", APPDATA_STORAGE, DIRECTORY) >> [
                aPreviewFile(10, "$APPDATA/preview/a/b/c/d/e/f/0/13/1000-563-max.jpg", IMAGE_JPEG, 28820L),
                aPreviewFile(11, "$APPDATA/preview/a/b/c/d/e/f/0/13/256-256-crop.jpg", IMAGE_JPEG, 9000L),
                aPreviewFile(12, "$APPDATA/preview/1/2/3/4/5/6/7/14/500-500-max.png", IMAGE_PNG, 50545L),
        ].stream()

        when:
        def thumbs = collect()

        then:
        thumbs == [
                new Thumbnail(13, "${nextcloudDir.absolutePath}/$APPDATA/preview/a/b/c/d/e/f/0/13/1000-563-max.jpg", JPEG, 28820L, 1000, 563),
                new Thumbnail(13, "${nextcloudDir.absolutePath}/$APPDATA/preview/a/b/c/d/e/f/0/13/256-256-crop.jpg", JPEG, 9000L, 256, 256),
                new Thumbnail(14, "${nextcloudDir.absolutePath}/$APPDATA/preview/1/2/3/4/5/6/7/14/500-500-max.png", PNG, 50545L, 500, 500),
        ]
    }

    def "skips files which are not recognizable previews"() {
        given:
        filecacheRepository.findThumbnails(*_) >> [
                aPreviewFile(10, "$APPDATA/preview/a/b/c/d/e/f/0/13/notes.jpg", IMAGE_JPEG, 1L),
                aPreviewFile(11, "$APPDATA/preview/a/b/c/d/e/f/0/13/256-256.txt", TEXT_PLAIN, 1L),
                aPreviewFile(12, "$APPDATA/preview/oc_previews_marker", IMAGE_JPEG, 1L),
        ].stream()

        expect:
        collect().isEmpty()
    }

    private List<Thumbnail> collect() {
        def thumbs = []
        sut.process { thumbs << it }
        thumbs
    }

    private static Filecache aPreviewFile(int id, String path, int mimetype, long size) {
        new Filecache(id, APPDATA_STORAGE, path, 0, new File(path).name, mimetype, size, 0L, 0L)
    }
}
