package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentGroup.*
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import net.schowek.nextclouddlna.util.ExternalUrls
import org.jupnp.support.model.DIDLObject
import org.jupnp.support.model.Res
import org.jupnp.support.model.WriteStatus.NOT_WRITABLE
import org.jupnp.support.model.container.Container
import org.jupnp.support.model.item.AudioItem
import org.jupnp.support.model.item.ImageItem
import org.jupnp.support.model.item.Item
import org.jupnp.support.model.item.VideoItem
import org.springframework.stereotype.Component


@Component
class NodeConverter(
    private val externalUrls: ExternalUrls,
    private val dlnaProtocolInfoBuilder: DlnaProtocolInfoBuilder
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
                        dlnaProtocolInfoBuilder.buildThumbnailProtocolInfo(t.format.asMimetype()),
                        t.fileLength,
                        externalUrls.contentUrl(t.id)
                    )
                )
            }
        }
    }
}

