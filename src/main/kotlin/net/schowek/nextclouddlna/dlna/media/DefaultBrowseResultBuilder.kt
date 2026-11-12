package net.schowek.nextclouddlna.dlna.media

import org.jupnp.support.contentdirectory.DIDLParser
import org.jupnp.support.model.BrowseResult
import org.jupnp.support.model.DIDLContent
import org.jupnp.support.model.container.Container
import org.jupnp.support.model.item.Item
import org.springframework.stereotype.Component
import kotlin.math.max
import kotlin.math.min

@Component
class DefaultBrowseResultBuilder(
    private val didlParser: DIDLParser
) : BrowseResultBuilder {
    override fun createBrowseResult(
        containers: List<Container>,
        items: List<Item>,
        firstResult: Long,
        maxResults: Long
    ): BrowseResult {
        val resolvedMax = if (maxResults == 0L) (containers.size + items.size).toLong() else maxResults
        val didl = DIDLContent()
        if (containers.size > firstResult) {
            val from = firstResult.toInt()
            val to = min((firstResult + resolvedMax).toInt(), containers.size)
            didl.containers = containers.subList(from, to)
        }
        if (didl.containers.size < resolvedMax) {
            val from = max(firstResult - containers.size, 0).toInt()
            val to = min(items.size, from + (resolvedMax - didl.containers.size).toInt())
            didl.items = items.subList(from, to)
        }
        return BrowseResult(
            didlParser.generate(didl),
            (didl.containers.size + didl.items.size).toLong(),
            (containers.size + items.size).toLong()
        )
    }
}
