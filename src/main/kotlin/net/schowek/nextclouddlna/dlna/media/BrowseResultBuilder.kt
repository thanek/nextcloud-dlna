package net.schowek.nextclouddlna.dlna.media

import org.jupnp.support.model.BrowseResult
import org.jupnp.support.model.container.Container
import org.jupnp.support.model.item.Item

interface BrowseResultBuilder {
    fun createBrowseResult(containers: List<Container>, items: List<Item>, firstResult: Long, maxResults: Long): BrowseResult
}
