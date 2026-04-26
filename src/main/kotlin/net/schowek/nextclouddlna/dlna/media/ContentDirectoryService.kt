package net.schowek.nextclouddlna.dlna.media

import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.content.ContentGroup
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import org.jupnp.support.contentdirectory.AbstractContentDirectoryService
import org.jupnp.support.contentdirectory.ContentDirectoryErrorCode
import org.jupnp.support.contentdirectory.ContentDirectoryException
import org.jupnp.support.contentdirectory.DIDLParser
import org.jupnp.support.model.BrowseFlag
import org.jupnp.support.model.BrowseFlag.*
import org.jupnp.support.model.BrowseResult
import org.jupnp.support.model.DIDLContent
import org.jupnp.support.model.SortCriterion
import org.jupnp.support.model.container.Container
import org.jupnp.support.model.item.Item
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit.NANOSECONDS
import kotlin.math.max
import kotlin.math.min


@Component
class ContentDirectoryService(
    private val contentTreeProvider: ContentTreeProvider,
    private val nodeConverter: NodeConverter
) : AbstractContentDirectoryService(
    mutableListOf("dc:title", "upnp:class"),
    mutableListOf("dc:title", "dc:date", "upnp:class")
) {

    /**
     * Root is requested with objectID="0".
     */
    @Throws(ContentDirectoryException::class)
    override fun browse(
        objectID: String,
        browseFlag: BrowseFlag,
        filter: String,
        firstResult: Long,
        maxResults: Long,
        orderby: Array<SortCriterion>
    ): BrowseResult {
        val startTime = System.nanoTime()
        return try {
            contentTreeProvider.getNode(objectID)?.let { node ->
                if (browseFlag == METADATA) {
                    val result = DIDLParser().generate(
                        DIDLContent().also {
                            it.addContainer(nodeConverter.makeContainerWithoutSubContainers(node))
                        }
                    )
                    return BrowseResult(result, 1, 1)
                }
                val containers: List<Container> = sortNodes(node.nodes, orderby)
                    .map { nodeConverter.makeContainerWithoutSubContainers(it) }
                val items: List<Item> = sortItems(node.items, orderby)
                    .map { nodeConverter.makeItem(it) }
                return toRangedResult(containers, items, firstResult, maxResults)
            }

            contentTreeProvider.getItem(objectID)?.let { item ->
                val result = DIDLParser().generate(
                    DIDLContent().also {
                        it.addItem(nodeConverter.makeItem(item))
                    }
                )
                return BrowseResult(result, 1, 1)
            }

            BrowseResult(DIDLParser().generate(DIDLContent()), 0, 0)
        } catch (e: Exception) {
            logger.warn(
                "Failed to generate directory listing (objectID={}, browseFlag={}, filter={}, firstResult={}, maxResults={}, orderby={}).",
                objectID, browseFlag, filter, firstResult, maxResults, Arrays.toString(orderby), e
            )
            throw ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, e.toString())
        } finally {
            logger.info(
                "Browse: {} ({}, {}) in {}ms.",
                objectID, firstResult, maxResults, NANOSECONDS.toMillis(System.nanoTime() - startTime)
            )
        }
    }
    @Throws(Exception::class)
    private fun toRangedResult(
        containers: List<Container>,
        items: List<Item>,
        firstResult: Long,
        maxResultsParam: Long
    ): BrowseResult {
        val maxResults = if (maxResultsParam == 0L) (containers.size + items.size).toLong() else maxResultsParam
        val didl = DIDLContent()
        if (containers.size > firstResult) {
            val from = firstResult.toInt()
            val to = min((firstResult + maxResults).toInt(), containers.size)
            didl.containers = containers.subList(from, to)
        }
        if (didl.containers.size < maxResults) {
            val from = max(firstResult - containers.size, 0).toInt()
            val to = min(items.size, from + (maxResults - didl.containers.size).toInt())
            didl.items = items.subList(from, to)
        }
        return BrowseResult(
            DIDLParser().generate(didl),
            (didl.containers.size + didl.items.size).toLong(),
            (containers.size + items.size).toLong()
        )
    }

    private fun sortNodes(nodes: List<ContentNode>, orderby: Array<SortCriterion>): List<ContentNode> {
        var comparator: Comparator<ContentNode>? = null
        for (criterion in orderby) {
            val c: Comparator<ContentNode> = when (criterion.propertyName) {
                "dc:title" -> compareBy { it.name.lowercase() }
                else -> continue
            }
            val ordered = if (criterion.isAscending) c else c.reversed()
            comparator = comparator?.thenComparing(ordered) ?: ordered
        }
        return comparator?.let { nodes.sortedWith(it) } ?: nodes
    }

    private fun sortItems(items: List<ContentItem>, orderby: Array<SortCriterion>): List<ContentItem> {
        var comparator: Comparator<ContentItem>? = null
        for (criterion in orderby) {
            val c: Comparator<ContentItem> = when (criterion.propertyName) {
                "dc:title" -> compareBy { it.name.lowercase() }
                "dc:date" -> compareBy { it.mtime }
                "upnp:class" -> compareBy { upnpClassOf(it) }
                else -> continue
            }
            val ordered = if (criterion.isAscending) c else c.reversed()
            comparator = comparator?.thenComparing(ordered) ?: ordered
        }
        return comparator?.let { items.sortedWith(it) } ?: items
    }

    private fun upnpClassOf(item: ContentItem): String = when (item.format.contentGroup) {
        ContentGroup.VIDEO -> "object.item.videoItem"
        ContentGroup.IMAGE -> "object.item.imageItem"
        ContentGroup.AUDIO -> "object.item.audioItem"
        else -> "object.item"
    }

    companion object : KLogging()
}

