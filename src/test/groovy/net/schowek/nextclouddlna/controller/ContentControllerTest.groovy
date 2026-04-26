package net.schowek.nextclouddlna.controller

import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import spock.lang.Specification

class ContentControllerTest extends Specification {
    def contentTreeProvider = Mock(ContentTreeProvider)
    def sut = new ContentController(contentTreeProvider)

    // makeProtocolInfo is private in Kotlin, but Groovy ignores visibility for testing purposes

    def "makeProtocolInfo for video: only MP4/M4V get AVC_MP4_LPCM, all video gets streaming flags"() {
        when:
        def info = sut.makeProtocolInfo(format)

        then:
        info.contentFormat == format.mime
        info.toString().contains("DLNA.ORG_FLAGS")
        info.toString().contains("DLNA.ORG_PN=AVC_MP4_LPCM") == hasProfile

        where:
        format          || hasProfile
        MediaFormat.MP4 || true
        MediaFormat.M4V || true
        MediaFormat.MKV || false
        MediaFormat.AVI || false
        MediaFormat.WMV || false
        MediaFormat.WEBM|| false
    }

    def "makeProtocolInfo for audio: format-specific DLNA profile and streaming flags"() {
        when:
        def info = sut.makeProtocolInfo(format)

        then:
        info.contentFormat == format.mime
        info.toString().contains("DLNA.ORG_FLAGS")
        expectedProfile ? info.toString().contains("DLNA.ORG_PN=$expectedProfile") : !info.toString().contains("DLNA.ORG_PN=")

        where:
        format            || expectedProfile
        MediaFormat.MP3   || "MP3"
        MediaFormat.MPGA  || "MP3"
        MediaFormat.AAC   || "AAC_ISO"
        MediaFormat.M4A   || "AAC_ISO"
        MediaFormat.WMA   || "WMABASE"
        MediaFormat.WAV   || "LPCM"
        MediaFormat.FLAC  || null
        MediaFormat.OGG   || null
    }

    def "makeProtocolInfo for image: format-specific DLNA profile, no streaming flags"() {
        when:
        def info = sut.makeProtocolInfo(format)

        then:
        info.contentFormat == format.mime
        !info.toString().contains("DLNA.ORG_FLAGS")
        expectedProfile ? info.toString().contains("DLNA.ORG_PN=$expectedProfile") : !info.toString().contains("DLNA.ORG_PN=")

        where:
        format            || expectedProfile
        MediaFormat.JPEG  || "JPEG_LRG"
        MediaFormat.JPG   || "JPEG_LRG"
        MediaFormat.PNG   || "PNG_LRG"
        MediaFormat.GIF   || null
        MediaFormat.WEBP  || null
    }
}
