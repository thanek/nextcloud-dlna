package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import net.schowek.nextclouddlna.nextcloud.content.Thumbnail
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import java.sql.SQLException

import static net.schowek.nextclouddlna.nextcloud.content.MediaFormat.JPEG
import static net.schowek.nextclouddlna.nextcloud.content.MediaFormat.PNG

class PreviewTableThumbnailProcessorTest extends Specification {
    static final int IMAGE_JPEG = 2
    static final int IMAGE_PNG = 3
    static final int IMAGE_SVG = 4
    static final String APPDATA = "appdata_oc123"

    def nextcloudDir = new File("/var/www/nextcloud/data")
    def configDiscovery = Mock(NextcloudConfigDiscovery)
    def previewRepository = Mock(PreviewRepository)
    def mimetypeRepository = Mock(MimetypeRepository)
    def jdbcTemplate = Mock(JdbcTemplate)
    def mimetypeResolver

    def setup() {
        configDiscovery.getAppDataDir() >> APPDATA
        configDiscovery.getNextcloudDir() >> nextcloudDir
        mimetypeRepository.findAll() >> [
                new Mimetype(1, 'httpd/unix-directory'),
                new Mimetype(IMAGE_JPEG, 'image/jpeg'),
                new Mimetype(IMAGE_PNG, 'image/png'),
                new Mimetype(IMAGE_SVG, 'image/svg+xml')
        ]
        mimetypeResolver = new DefaultMimetypeResolver(mimetypeRepository)
    }

    def "builds the sharded on-disk path Nextcloud uses for previews"() {
        given:
        previewRepository.findLocalUnversioned() >> [
                aPreview(397, 256, 256, IMAGE_JPEG, true, false, 9000L),
                aPreview(397, 4096, 2731, IMAGE_JPEG, false, true, 900000L),
                aPreview(1666451, 1024, 768, IMAGE_PNG, false, false, 70000L),
        ].stream()

        when:
        def thumbs = collect(aProcessor())

        then: "shard dirs are the first 7 hex chars of md5(fileId)"
        thumbs == [
                new Thumbnail(397, "${previewRoot()}/e/4/6/d/e/7/e/397/256-256-crop.jpg", JPEG, 9000L, 256, 256),
                new Thumbnail(397, "${previewRoot()}/e/4/6/d/e/7/e/397/4096-2731-max.jpg", JPEG, 900000L, 4096, 2731),
                new Thumbnail(1666451, "${previewRoot()}/${shards("1666451")}/1666451/1024-768.png", PNG, 70000L, 1024, 768),
        ]
    }

    def "skips previews with a mimetype unknown to DLNA"() {
        given:
        previewRepository.findLocalUnversioned() >> [aPreview(397, 256, 256, IMAGE_SVG, false, false, 10L)].stream()

        expect:
        collect(aProcessor()).isEmpty()
    }

    def "does nothing on Nextcloud versions without the previews table"() {
        given:
        jdbcTemplate.queryForList(_ as String) >> { throw new BadSqlGrammarException("probe", "SELECT", new SQLException("no table")) }
        def sut = new PreviewTableThumbnailProcessor(configDiscovery, previewRepository, mimetypeResolver, jdbcTemplate)

        when:
        def thumbs = collect(sut)

        then:
        thumbs.isEmpty()
        0 * previewRepository.findLocalUnversioned()
    }

    private PreviewTableThumbnailProcessor aProcessor() {
        jdbcTemplate.queryForList(_ as String) >> []
        new PreviewTableThumbnailProcessor(configDiscovery, previewRepository, mimetypeResolver, jdbcTemplate)
    }

    private String previewRoot() {
        "${nextcloudDir.absolutePath}/$APPDATA/preview"
    }

    private static String shards(String fileId) {
        fileId.md5().take(7).toList().join("/")
    }

    private static List<Thumbnail> collect(PreviewTableThumbnailProcessor processor) {
        def thumbs = []
        processor.process { thumbs << it }
        thumbs
    }

    private static Preview aPreview(long fileId, int width, int height, int mimetypeId, boolean cropped, boolean max, long size) {
        new Preview(fileId * 1000 + width, fileId, null, width, height, mimetypeId, max, cropped, size, -1L)
    }
}
