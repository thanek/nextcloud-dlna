package net.schowek.nextclouddlna.dlna.media

import mu.KLogging
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
import java.util.*
import java.util.concurrent.TimeUnit.NANOSECONDS


class ContentDirectoryService(
    private val contentTreeProvider: ContentTreeProvider,
    private val nodeConverter: NodeConverter,
    private val didlParser: DIDLParser,
    private val resultSorter: ResultSorter,
    private val browseResultBuilder: BrowseResultBuilder
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
                    val result = didlParser.generate(
                        DIDLContent().also {
                            it.addContainer(nodeConverter.makeContainerWithoutSubContainers(node))
                        }
                    )
                    return BrowseResult(result, 1, 1)
                }
                val containers: List<Container> = resultSorter.sortNodes(node.nodes, orderby)
                    .map { nodeConverter.makeContainerWithoutSubContainers(it) }
                val items: List<Item> = resultSorter.sortItems(node.items, orderby)
                    .map { nodeConverter.makeItem(it) }
                return browseResultBuilder.createBrowseResult(containers, items, firstResult, maxResults)
            }

            contentTreeProvider.getItem(objectID)?.let { item ->
                val result = didlParser.generate(
                    DIDLContent().also {
                        it.addItem(nodeConverter.makeItem(item))
                    }
                )
                return BrowseResult(result, 1, 1)
            }

            BrowseResult(didlParser.generate(DIDLContent()), 0, 0)
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

    companion object : KLogging()
}

