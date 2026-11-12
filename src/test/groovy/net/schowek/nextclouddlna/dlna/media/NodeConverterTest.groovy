package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import net.schowek.nextclouddlna.nextcloud.content.Thumbnail
import net.schowek.nextclouddlna.util.ExternalUrls
import org.jupnp.support.model.DIDLObject
import org.jupnp.support.model.dlna.DLNAProfiles
import org.jupnp.support.model.item.AudioItem
import org.jupnp.support.model.item.ImageItem
import org.jupnp.support.model.item.VideoItem
import spock.lang.Specification

class NodeConverterTest extends Specification {
    def externalUrls = Mock(ExternalUrls)
    def dlnaProtocolInfoBuilder = Mock(DlnaProtocolInfoBuilder)
    def dlnaProfileResolver = Mock(DlnaProfileResolver)
    def sut = new NodeConverter(externalUrls, dlnaProtocolInfoBuilder, dlnaProfileResolver)

    void setup() {
        externalUrls.contentUrl(_ as Integer) >> { int id -> "http://test/c/$id" }
        externalUrls.thumbnailUrl(_ as Integer) >> { int id -> "http://test/t/$id" }
        dlnaProfileResolver.resolveThumbnailProfile("image/jpeg") >> DLNAProfiles.JPEG_TN
        dlnaProtocolInfoBuilder.buildThumbnailProtocolInfo(_) >> { mimeType ->
            def mock = Mock(org.jupnp.support.model.dlna.DLNAProtocolInfo)
            mock.toString() >> "JPEG_TN"
            mock
        }
    }

    def "makeItem creates VideoItem for video format"() {
        given:
        def item = new ContentItem(1, 0, "movie.mp4", "/path/movie.mp4", MediaFormat.MP4, 5000L, 0L)

        when:
        def result = sut.makeItem(item)

        then:
        result instanceof VideoItem
        result.id == "1"
        result.parentID == "0"
        result.title == "movie.mp4"
        result.resources.size() == 1
        result.resources[0].value == "http://test/c/1"
        result.resources[0].size == 5000L
    }

    def "makeItem creates AudioItem for audio format"() {
        given:
        def item = new ContentItem(2, 0, "song.mp3", "/path/song.mp3", MediaFormat.MP3, 3000L, 0L)

        when:
        def result = sut.makeItem(item)

        then:
        result instanceof AudioItem
        result.id == "2"
        result.title == "song.mp3"
    }

    def "makeItem creates ImageItem for image format"() {
        given:
        def item = new ContentItem(3, 0, "photo.jpg", "/path/photo.jpg", MediaFormat.JPEG, 200L, 0L)

        when:
        def result = sut.makeItem(item)

        then:
        result instanceof ImageItem
        result.id == "3"
        result.title == "photo.jpg"
    }

    def "makeItem includes thumbnail as second resource with DLNA thumbnail profile"() {
        given:
        def item = new ContentItem(3, 0, "photo.jpg", "/path/photo.jpg", MediaFormat.JPEG, 200L, 0L)
        item.thumb = new Thumbnail(3, "/thumb/256-256.jpg", MediaFormat.JPEG, 50L, 256, 256)

        when:
        def result = sut.makeItem(item)

        then:
        result.resources.size() == 2
        result.resources[0].value == "http://test/c/3"
        result.resources[1].value == "http://test/t/3"
        result.resources[1].size == 50L
        result.resources[1].protocolInfo.toString().contains("JPEG")
    }

    def "makeItem exposes thumbnail as album art with DLNA profile id"() {
        given:
        def item = new ContentItem(3, 0, "movie.mp4", "/path/movie.mp4", MediaFormat.MP4, 200L, 0L)
        item.thumb = new Thumbnail(3, "/thumb/256-256.jpg", MediaFormat.JPEG, 50L, 256, 256)

        when:
        def albumArt = sut.makeItem(item).getLastProperty(DIDLObject.Property.UPNP.ALBUM_ART_URI)

        then:
        albumArt.value.toString() == "http://test/t/3"
        albumArt.getAttribute("profileID").value.value == "JPEG_TN"
    }

    def "makeItem without thumbnail has single resource"() {
        given:
        def item = new ContentItem(1, 0, "movie.mp4", "/path/movie.mp4", MediaFormat.MP4, 5000L, 0L)

        when:
        def result = sut.makeItem(item)

        then:
        result.resources.size() == 1
        !result.hasProperty(DIDLObject.Property.UPNP.ALBUM_ART_URI)
    }

    def "makeContainerWithoutSubContainers maps node fields correctly"() {
        given:
        def node = new ContentNode(1, 0, "Videos")
        node.addNode(new ContentNode(2, 1, "Action"))
        node.addItem(new ContentItem(3, 1, "movie.mp4", "/path", MediaFormat.MP4, 100L, 0L))

        when:
        def result = sut.makeContainerWithoutSubContainers(node)

        then:
        result.id == "1"
        result.parentID == "0"
        result.title == "Videos"
        result.childCount == 2
        result.isRestricted()
        result.isSearchable()
    }
}
