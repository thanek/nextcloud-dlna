package net.schowek.nextclouddlna.dlna.media

import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.content.ContentGroup
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import org.jupnp.support.model.SortCriterion
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DefaultResultSorter(
    @Value("\${server.defaultSortCriteria}")
    defaultSortCriteria: String
) : ResultSorter {
    private val criteriaForClientsThatRequestNoOrder = parseDefaultCriteria(defaultSortCriteria)

    override fun sortNodes(nodes: List<ContentNode>, orderby: Array<SortCriterion>): List<ContentNode> {
        return sortWith(nodes, orderby, { it.id }, whenNoCriterionApplies = byName()) { propName, ascending ->
            val c: Comparator<ContentNode>? = when (propName) {
                "dc:title" -> byName()
                else -> null
            }
            c?.let { if (ascending) it else it.reversed() }
        }
    }

    override fun sortItems(items: List<ContentItem>, orderby: Array<SortCriterion>): List<ContentItem> {
        return sortWith(items, orderby, { it.id }) { propName, ascending ->
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
        idOf: (T) -> Int,
        whenNoCriterionApplies: Comparator<T>? = null,
        comparatorFor: (String, Boolean) -> Comparator<T>?
    ): List<T> {
        val criteria = orderby.ifEmpty { criteriaForClientsThatRequestNoOrder }
        var comparator: Comparator<T>? = null
        for (criterion in criteria) {
            comparatorFor(criterion.propertyName, criterion.isAscending)
                ?.let { c -> comparator = comparator?.thenComparing(c) ?: c }
        }
        if (comparator == null && criteria.isNotEmpty()) {
            comparator = whenNoCriterionApplies
        }
        val sameOrderAfterEveryTreeRebuild: Comparator<T> = compareBy(idOf)
        return comparator?.let { items.sortedWith(it.thenComparing(sameOrderAfterEveryTreeRebuild)) } ?: items
    }

    private fun parseDefaultCriteria(defaultSortCriteria: String): Array<SortCriterion> {
        val criteria = defaultSortCriteria.trim()
        if (criteria.isEmpty()) return emptyArray()
        return try {
            SortCriterion.valueOf(criteria).also {
                it.filterNot { c -> SUPPORTED_PROPERTIES.contains(c.propertyName) }
                    .forEach { c -> logger.warn("Unsupported default sort property: {}. Ignoring it.", c.propertyName) }
                logger.info("Browse requests without sort criteria will be sorted by: {}", SortCriterion.toString(it))
            }
        } catch (e: IllegalArgumentException) {
            logger.warn(
                "Invalid default sort criteria: '{}' ({}). Falling back to '{}'.",
                criteria, e.message, FALLBACK_SORT_CRITERIA
            )
            SortCriterion.valueOf(FALLBACK_SORT_CRITERIA)
        }
    }

    private fun byName(): Comparator<ContentNode> = compareBy { it.name.lowercase() }

    private fun upnpClassOf(item: ContentItem): String = when (item.format.contentGroup) {
        ContentGroup.VIDEO -> "object.item.videoItem"
        ContentGroup.IMAGE -> "object.item.imageItem"
        ContentGroup.AUDIO -> "object.item.audioItem"
        else -> "object.item"
    }

    companion object : KLogging() {
        const val FALLBACK_SORT_CRITERIA = "+dc:title"
        private val SUPPORTED_PROPERTIES = setOf("dc:title", "dc:date", "upnp:class")
    }
}
