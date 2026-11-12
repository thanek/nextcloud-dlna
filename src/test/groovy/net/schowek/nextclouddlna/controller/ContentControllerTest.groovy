package net.schowek.nextclouddlna.controller

import net.schowek.nextclouddlna.dlna.media.DlnaProtocolInfoBuilder
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import net.schowek.nextclouddlna.nextcloud.content.Thumbnail
import org.jupnp.support.model.dlna.DLNAProtocolInfo
import org.springframework.core.io.FileSystemResource
import spock.lang.Specification

class ContentControllerTest extends Specification {

    def contentTreeProvider = Mock(ContentTreeProvider)
    def dlnaProtocolInfoBuilder = Mock(DlnaProtocolInfoBuilder)
    def sut = new ContentController(contentTreeProvider, dlnaProtocolInfoBuilder)

    def "getResource returns NOT_FOUND when item not found"() {
        given:
        def request = Mock(jakarta.servlet.http.HttpServletRequest)
        def response = Mock(jakarta.servlet.http.HttpServletResponse)
        def rangeHeaders = Mock(Enumeration)
        request.getHeaders("range") >> rangeHeaders
        rangeHeaders.hasMoreElements() >> false

        when:
        def result = sut.getResource("999", request, response)

        then:
        result.statusCode.value == 404
    }

    def "getResource returns file with correct headers when item found"() {
        given:
        def item = new ContentItem(1, 0, "movie.mp4", "/tmp/test-movie.mp4", MediaFormat.MP4, 5000L, 0L)
        def request = Mock(jakarta.servlet.http.HttpServletRequest)
        def response = Mock(jakarta.servlet.http.HttpServletResponse)
        def protocolInfo = Mock(DLNAProtocolInfo)
        def rangeHeaders = Mock(Enumeration)
        def file = new File("/tmp/test-movie.mp4")
        file.createNewFile()

        contentTreeProvider.getItem("1") >> item
        dlnaProtocolInfoBuilder.build(MediaFormat.MP4) >> protocolInfo
        request.getHeaders("range") >> rangeHeaders
        rangeHeaders.hasMoreElements() >> false

        when:
        def result = sut.getResource("1", request, response)

        then:
        result.statusCode.value == 200
        result.body instanceof FileSystemResource
        1 * response.addHeader("Content-Type", "video/mp4")
        1 * response.addHeader("contentFeatures.dlna.org", _)
        1 * response.addHeader("transferMode.dlna.org", "Streaming")
        1 * response.addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*")

        cleanup:
        file.delete()
    }

    def "getThumbnail serves the item's thumbnail file"() {
        given:
        def file = File.createTempFile("thumb", ".jpg")
        def item = new ContentItem(3, 0, "photo.jpg", "/photo.jpg", MediaFormat.JPEG, 200L, 0L)
        item.thumb = new Thumbnail(3, file.absolutePath, MediaFormat.JPEG, 50L, 256, 256)
        def response = Mock(jakarta.servlet.http.HttpServletResponse)
        def protocolInfo = Mock(DLNAProtocolInfo)
        protocolInfo.toString() >> "DLNA.ORG_PN=JPEG_TN"
        contentTreeProvider.getItem("3") >> item
        dlnaProtocolInfoBuilder.buildThumbnailProtocolInfo(MediaFormat.JPEG.asMimetype()) >> protocolInfo

        when:
        def result = sut.getThumbnail("3", response)

        then:
        result.statusCode.value == 200
        result.body.path == file.absolutePath
        1 * response.addHeader("Content-Type", "image/jpeg")
        1 * response.addHeader("contentFeatures.dlna.org", "DLNA.ORG_PN=JPEG_TN")
        1 * response.addHeader("transferMode.dlna.org", "Interactive")

        cleanup:
        file.delete()
    }

    def "getThumbnail returns NOT_FOUND when item has no thumbnail or its file is missing"() {
        given:
        def withoutThumb = new ContentItem(1, 0, "song.mp3", "/song.mp3", MediaFormat.MP3, 1L, 0L)
        def withMissingFile = new ContentItem(2, 0, "photo.jpg", "/photo.jpg", MediaFormat.JPEG, 1L, 0L)
        withMissingFile.thumb = new Thumbnail(2, "/nonexistent/256-256.jpg", MediaFormat.JPEG, 1L, 256, 256)
        contentTreeProvider.getItem("1") >> withoutThumb
        contentTreeProvider.getItem("2") >> withMissingFile

        expect:
        sut.getThumbnail(id, Mock(jakarta.servlet.http.HttpServletResponse)).statusCode.value == 404

        where:
        id << ["1", "2", "999"]
    }
}
