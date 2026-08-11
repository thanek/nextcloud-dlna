package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentGroup
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import org.jupnp.support.model.SortCriterion
import org.springframework.stereotype.Component

@Component
class DefaultResultSorter : ResultSorter {

    override fun sortNodes(nodes: List<ContentNode>, orderby: Array<SortCriterion>): List<ContentNode> {
        return sortWith(nodes, orderby) { propName, ascending ->
            val c: Comparator<ContentNode>? = when (propName) {
                "dc:title" -> compareBy { it.name.lowercase() }
                else -> null
            }
            c?.let { if (ascending) it else it.reversed() }
        }
    }

    override fun sortItems(items: List<ContentItem>, orderby: Array<SortCriterion>): List<ContentItem> {
        return sortWith(items, orderby) { propName, ascending ->
            val c: Comparator<ContentItem> = when (propName) {
                "dc:title" -> compareBy { it.name.lowercase() }
                "dc:date" -> compareBy { it.mtime }
                "upnp:class" -> compareBy { upnpClassOf(it) }
                else -> return@sortWith null
            }
            if (ascending) c else c.reversed()
        }
    }

    private fun <T> sortWith(
        items: List<T>,
        orderby: Array<SortCriterion>,
        comparatorFor: (String, Boolean) -> Comparator<T>?
    ): List<T> {
        var comparator: Comparator<T>? = null
        for (criterion in orderby) {
            comparatorFor(criterion.propertyName, criterion.isAscending)
                ?.let { c -> comparator = comparator?.thenComparing(c) ?: c }
        }
        return comparator?.let { items.sortedWith(it) } ?: items
    }

    private fun upnpClassOf(item: ContentItem): String = when (item.format.contentGroup) {
        ContentGroup.VIDEO -> "object.item.videoItem"
        ContentGroup.IMAGE -> "object.item.imageItem"
        ContentGroup.AUDIO -> "object.item.audioItem"
        else -> "object.item"
    }
}
