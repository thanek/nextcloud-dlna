package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentGroup
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import org.jupnp.support.model.Protocol
import org.jupnp.support.model.ProtocolInfo
import org.jupnp.support.model.dlna.*
import org.jupnp.support.model.dlna.DLNAAttribute
import org.jupnp.support.model.dlna.DLNAAttribute.Type
import org.jupnp.support.model.dlna.DLNAAttribute.Type.*
import org.jupnp.support.model.dlna.DLNAConversionIndicator.NONE
import org.jupnp.support.model.dlna.DLNAFlags.*
import org.jupnp.support.model.dlna.DLNAOperations.*
import org.jupnp.support.model.dlna.DLNAProfileAttribute
import org.jupnp.support.model.dlna.DLNAProfiles
import org.jupnp.util.MimeType
import org.springframework.stereotype.Component
import java.util.EnumMap


@Component
class DlnaProtocolInfoBuilder(
    private val profileResolver: DlnaProfileResolver
) {

    fun build(mediaFormat: MediaFormat): DLNAProtocolInfo {
        val attributes = EnumMap<Type, DLNAAttribute<*>>(Type::class.java)
        when (mediaFormat.contentGroup) {
            ContentGroup.VIDEO -> buildVideoAttributes(attributes, mediaFormat)
            ContentGroup.AUDIO -> buildAudioAttributes(attributes, mediaFormat)
            ContentGroup.IMAGE -> buildImageAttributes(attributes, mediaFormat)
            else -> {}
        }
        return DLNAProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, mediaFormat.mime, attributes)
    }

    fun buildThumbnailProtocolInfo(mimeType: MimeType): DLNAProtocolInfo {
        val attributes = EnumMap<Type, DLNAAttribute<*>>(Type::class.java)
        profileResolver.resolveThumbnailProfile(mimeType.toString())?.let {
            attributes[Type.DLNA_ORG_PN] = DLNAProfileAttribute(it)
        }
        return DLNAProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, mimeType.toString(), attributes)
    }

    private fun buildVideoAttributes(attributes: EnumMap<Type, DLNAAttribute<*>>, format: MediaFormat) {
        val streamingFlags = DLNAFlagsAttribute(
            INTERACTIVE_TRANSFERT_MODE, BACKGROUND_TRANSFERT_MODE, DLNA_V15, STREAMING_TRANSFER_MODE
        )
        profileResolver.resolveVideoProfile(format)?.let { attributes[DLNA_ORG_PN] = DLNAProfileAttribute(it) }
        attributes[DLNA_ORG_OP] = DLNAOperationsAttribute(RANGE)
        attributes[DLNA_ORG_CI] = DLNAConversionIndicatorAttribute(DLNAConversionIndicator.NONE)
        attributes[DLNA_ORG_FLAGS] = streamingFlags
    }

    private fun buildAudioAttributes(attributes: EnumMap<Type, DLNAAttribute<*>>, format: MediaFormat) {
        val streamingFlags = DLNAFlagsAttribute(
            INTERACTIVE_TRANSFERT_MODE, BACKGROUND_TRANSFERT_MODE, DLNA_V15, STREAMING_TRANSFER_MODE
        )
        profileResolver.resolveAudioProfile(format)?.let { attributes[DLNA_ORG_PN] = DLNAProfileAttribute(it) }
        attributes[DLNA_ORG_OP] = DLNAOperationsAttribute(RANGE)
        attributes[DLNA_ORG_FLAGS] = streamingFlags
    }

    private fun buildImageAttributes(attributes: EnumMap<Type, DLNAAttribute<*>>, format: MediaFormat) {
        profileResolver.resolveImageProfile(format)?.let { attributes[DLNA_ORG_PN] = DLNAProfileAttribute(it) }
    }
}
