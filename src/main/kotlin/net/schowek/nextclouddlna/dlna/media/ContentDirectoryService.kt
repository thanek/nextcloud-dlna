package net.schowek.nextclouddlna.dlna.media

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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit.NANOSECONDS


@Component
class ContentDirectoryService(
    private val contentTreeProvider: ContentTreeProvider,
    private val nodeConverter: NodeConverter
) :
    AbstractContentDirectoryService(
        mutableListOf("dc:title", "upnp:class"),  // also "dc:creator", "dc:date", "res@size"
        mutableListOf("dc:title")
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
            // TODO optimize:
            // * checking if it's node or item before fetching them in two queries
            // * not fetching children for METADATA browse flag
            val node = contentTreeProvider.getNode(objectID)
            if (node != null) {
                if (browseFlag == METADATA) {
                    val didl = DIDLContent()
                    didl.addContainer(nodeConverter.makeContainerWithoutSubContainers(node))
                    return BrowseResult(DIDLParser().generate(didl), 1, 1)
                }
                val containers: List<Container> = nodeConverter.makeSubContainersWithoutTheirSubContainers(node)
                val items: List<Item> = nodeConverter.makeItems(node)
                return toRangedResult(containers, items, firstResult, maxResults)
            }
            val item = contentTreeProvider.getItem(objectID)
            if (item != null) {
                val didl = DIDLContent()
                didl.addItem(nodeConverter.makeItem(item))
                val result = DIDLParser().generate(didl)
                return BrowseResult(result, 1, 1)
            }
            BrowseResult(DIDLParser().generate(DIDLContent()), 0, 0)
        } catch (e: Exception) {
            LOG.warn(
                String.format(
                    "Failed to generate directory listing" +
                            " (objectID=%s, browseFlag=%s, filter=%s, firstResult=%s, maxResults=%s, orderby=%s).",
                    objectID, browseFlag, filter, firstResult, maxResults, Arrays.toString(orderby)
                ), e
            )
            throw ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS, e.toString())
        } finally {
            LOG.info(
                "Browse: {} ({}, {}) in {}ms.",
                objectID, firstResult, maxResults,
                NANOSECONDS.toMillis(System.nanoTime() - startTime)
            )
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ContentDirectoryService::class.java)

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
                val to = Math.min((firstResult + maxResults).toInt(), containers.size)
                didl.containers = containers.subList(from, to)
            }
            if (didl.containers.size < maxResults) {
                val from = Math.max(firstResult - containers.size, 0).toInt()
                val to = Math.min(items.size, from + (maxResults - didl.containers.size).toInt())
                didl.items = items.subList(from, to)
            }
            return BrowseResult(
                DIDLParser().generate(didl),
                (didl.containers.size + didl.items.size).toLong(),
                (containers.size + items.size).toLong()
            )
        }
    }
}

