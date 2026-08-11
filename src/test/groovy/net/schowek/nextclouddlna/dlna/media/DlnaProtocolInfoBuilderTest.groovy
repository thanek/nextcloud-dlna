package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import org.jupnp.support.model.dlna.DLNAProfiles
import spock.lang.Specification

class DlnaProtocolInfoBuilderTest extends Specification {

    def profileResolver = Mock(DlnaProfileResolver)
    def sut = new DlnaProtocolInfoBuilder(profileResolver)

    def "build for video: only MP4/M4V get AVC_MP4_LPCM, all video gets streaming flags"() {
        given:
        profileResolver.resolveVideoProfile(format) >> expectedProfile

        when:
        def info = sut.build(format)

        then:
        info.contentFormat == format.mime
        info.toString().contains("DLNA.ORG_FLAGS")
        info.toString().contains("DLNA.ORG_PN=AVC_MP4_LPCM") == hasProfile

        where:
        format          || hasProfile             || expectedProfile
        MediaFormat.MP4 || true                   || DLNAProfiles.AVC_MP4_LPCM
        MediaFormat.M4V || true                   || DLNAProfiles.AVC_MP4_LPCM
        MediaFormat.MKV || false                  || null
        MediaFormat.AVI || false                  || null
        MediaFormat.WMV || false                  || null
        MediaFormat.WEBM|| false                  || null
    }

    def "build for audio: format-specific DLNA profile and streaming flags"() {
        given:
        profileResolver.resolveAudioProfile(format) >> expectedProfile

        when:
        def info = sut.build(format)

        then:
        info.contentFormat == format.mime
        info.toString().contains("DLNA.ORG_FLAGS")
        expectedProfile ? info.toString().contains("DLNA.ORG_PN=" + expectedProfile.toString()) : !info.toString().contains("DLNA.ORG_PN=")

        where:
        format            || expectedProfile
        MediaFormat.MP3   || DLNAProfiles.MP3
        MediaFormat.MPGA  || DLNAProfiles.MP3
        MediaFormat.AAC   || DLNAProfiles.AAC_ISO
        MediaFormat.M4A   || DLNAProfiles.AAC_ISO
        MediaFormat.WMA   || DLNAProfiles.WMABASE
        MediaFormat.WAV   || DLNAProfiles.LPCM
        MediaFormat.FLAC  || null
        MediaFormat.OGG   || null
    }

    def "build for image: format-specific DLNA profile, no streaming flags"() {
        given:
        profileResolver.resolveImageProfile(format) >> expectedProfile

        when:
        def info = sut.build(format)

        then:
        info.contentFormat == format.mime
        !info.toString().contains("DLNA.ORG_FLAGS")
        expectedProfile ? info.toString().contains("DLNA.ORG_PN=" + expectedProfile.toString()) : !info.toString().contains("DLNA.ORG_PN=")

        where:
        format            || expectedProfile
        MediaFormat.JPEG  || DLNAProfiles.JPEG_LRG
        MediaFormat.JPG   || DLNAProfiles.JPEG_LRG
        MediaFormat.PNG   || DLNAProfiles.PNG_LRG
        MediaFormat.GIF   || null
        MediaFormat.WEBP  || null
    }

    def "buildThumbnailProtocolInfo returns JPEG_TN for JPEG"() {
        given:
        profileResolver.resolveThumbnailProfile("image/jpeg") >> DLNAProfiles.JPEG_TN

        when:
        def info = sut.buildThumbnailProtocolInfo(MediaFormat.JPEG.asMimetype())

        then:
        info.toString().contains("JPEG_TN")
    }

    def "buildThumbnailProtocolInfo returns PNG_TN for PNG"() {
        given:
        profileResolver.resolveThumbnailProfile("image/png") >> DLNAProfiles.PNG_TN

        when:
        def info = sut.buildThumbnailProtocolInfo(MediaFormat.PNG.asMimetype())

        then:
        info.toString().contains("PNG_TN")
    }
}
