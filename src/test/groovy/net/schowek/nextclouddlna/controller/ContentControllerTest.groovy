package net.schowek.nextclouddlna.controller

import net.schowek.nextclouddlna.dlna.media.DlnaProtocolInfoBuilder
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
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
}
