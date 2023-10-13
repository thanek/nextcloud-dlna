package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentGroup.*
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import net.schowek.nextclouddlna.util.ExternalUrls
import org.jupnp.support.model.DIDLObject
import org.jupnp.support.model.Protocol
import org.jupnp.support.model.ProtocolInfo
import org.jupnp.support.model.Res
import org.jupnp.support.model.WriteStatus.NOT_WRITABLE
import org.jupnp.support.model.container.Container
import org.jupnp.support.model.dlna.DLNAAttribute
import org.jupnp.support.model.dlna.DLNAProfileAttribute
import org.jupnp.support.model.dlna.DLNAProfiles
import org.jupnp.support.model.dlna.DLNAProfiles.JPEG_TN
import org.jupnp.support.model.dlna.DLNAProfiles.PNG_TN
import org.jupnp.support.model.dlna.DLNAProtocolInfo
import org.jupnp.support.model.item.AudioItem
import org.jupnp.support.model.item.ImageItem
import org.jupnp.support.model.item.Item
import org.jupnp.support.model.item.VideoItem
import org.jupnp.util.MimeType
import org.springframework.stereotype.Component
import java.util.*
import java.util.Collections.unmodifiableList


@Component
class NodeConverter(
    val externalUrls: ExternalUrls
) {
    fun makeSubContainersWithoutTheirSubContainers(n: ContentNode) =
        n.nodes.map { node -> makeContainerWithoutSubContainers(node) }.toList()

    fun makeContainerWithoutSubContainers(n: ContentNode) = Container().also { c ->
        c.clazz = DIDLObject.Class("object.container")
        c.id = "${n.id}"
        c.parentID = "${n.parentId}"
        c.title = n.name
        c.childCount = n.nodeAndItemCount
        c.isRestricted = true
        c.writeStatus = NOT_WRITABLE
        c.isSearchable = true
    }

    fun makeItems(n: ContentNode): List<Item> = n.items.map { item -> makeItem(item) }.toList()

    fun makeItem(c: ContentItem): Item {
        val res = Res(c.format.asMimetype(), c.fileLength, externalUrls.contentUrl(c.id))
        return when (c.format.contentGroup) {
            VIDEO -> VideoItem("${c.id}", "${c.parentId}", c.name, "", res)
            IMAGE -> ImageItem("${c.id}", "${c.parentId}", c.name, "", res)
            AUDIO -> AudioItem("${c.id}", "${c.parentId}", c.name, "", res)
            else -> throw IllegalArgumentException()
        }.also {
            c.thumb?.let { t ->
                it.addResource(
                    Res(
                        makeProtocolInfo(t.format.asMimetype()),
                        t.fileLength,
                        externalUrls.contentUrl(t.id)
                    )
                )
            }
        }
    }

    companion object {
        private val DLNA_THUMBNAIL_TYPES = unmodifiableList(listOf(JPEG_TN, PNG_TN))
        private val MIME_TYPE_TO_DLNA_THUMBNAIL_TYPE = DLNA_THUMBNAIL_TYPES.associateBy { it.contentFormat }

        private fun makeProtocolInfo(artMimeType: MimeType): DLNAProtocolInfo {
            val attributes = EnumMap<DLNAAttribute.Type, DLNAAttribute<*>>(
                DLNAAttribute.Type::class.java
            )
            findDlnaThumbnailProfile(artMimeType)?.let {
                attributes[DLNAAttribute.Type.DLNA_ORG_PN] = DLNAProfileAttribute(it)
            }
            return DLNAProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, artMimeType.toString(), attributes)
        }

        private fun findDlnaThumbnailProfile(mimeType: MimeType): DLNAProfiles? {
            return MIME_TYPE_TO_DLNA_THUMBNAIL_TYPE[mimeType.toString()]
        }
    }
}

