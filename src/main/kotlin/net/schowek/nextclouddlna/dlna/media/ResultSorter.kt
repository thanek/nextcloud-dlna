package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import org.jupnp.support.model.SortCriterion

interface ResultSorter {
    fun sortNodes(nodes: List<ContentNode>, orderby: Array<SortCriterion>): List<ContentNode>
    fun sortItems(items: List<ContentItem>, orderby: Array<SortCriterion>): List<ContentItem>
}
