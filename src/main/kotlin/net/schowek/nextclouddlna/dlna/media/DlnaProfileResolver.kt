package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import org.jupnp.support.model.dlna.DLNAProfiles
import org.springframework.stereotype.Component


interface DlnaProfileResolver {
    fun resolveVideoProfile(format: MediaFormat): DLNAProfiles?
    fun resolveAudioProfile(format: MediaFormat): DLNAProfiles?
    fun resolveImageProfile(format: MediaFormat): DLNAProfiles?
    fun resolveThumbnailProfile(mimeType: String): DLNAProfiles?
}

@Component
class DefaultDlnaProfileResolver : DlnaProfileResolver {

    private val videoProfiles = mapOf(
        MediaFormat.MP4 to DLNAProfiles.AVC_MP4_LPCM,
        MediaFormat.M4V to DLNAProfiles.AVC_MP4_LPCM,
    )

    private val audioProfiles = mapOf(
        MediaFormat.MP3 to DLNAProfiles.MP3,
        MediaFormat.MPGA to DLNAProfiles.MP3,
        MediaFormat.AAC to DLNAProfiles.AAC_ISO,
        MediaFormat.M4A to DLNAProfiles.AAC_ISO,
        MediaFormat.WMA to DLNAProfiles.WMABASE,
        MediaFormat.WAV to DLNAProfiles.LPCM,
    )

    private val imageProfiles = mapOf(
        MediaFormat.JPEG to DLNAProfiles.JPEG_LRG,
        MediaFormat.JPG to DLNAProfiles.JPEG_LRG,
        MediaFormat.PNG to DLNAProfiles.PNG_LRG,
    )

    private val thumbnailProfiles = mapOf(
        DLNAProfiles.JPEG_TN.contentFormat to DLNAProfiles.JPEG_TN,
        DLNAProfiles.PNG_TN.contentFormat to DLNAProfiles.PNG_TN,
    )

    override fun resolveVideoProfile(format: MediaFormat): DLNAProfiles? = videoProfiles[format]
    override fun resolveAudioProfile(format: MediaFormat): DLNAProfiles? = audioProfiles[format]
    override fun resolveImageProfile(format: MediaFormat): DLNAProfiles? = imageProfiles[format]
    override fun resolveThumbnailProfile(mimeType: String): DLNAProfiles? = thumbnailProfiles[mimeType]
}
