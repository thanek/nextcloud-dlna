package net.schowek.nextclouddlna.nextcloud.content

import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.NextcloudDB
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern


@Component
class ContentTreeProvider(
    private val nextcloudDB: NextcloudDB
) {
    private var tree = buildContentTree()
    private var lastMTime = 0L

    @Scheduled(fixedDelay = REBUILD_TREE_DELAY_IN_MS, initialDelay = REBUILD_TREE_INIT_DELAY_IN_MS)
    final fun rebuildTree() {
        val maxMtime: Long = nextcloudDB.maxMtime()
        val now = Instant.now().epochSecond
        if (lastMTime < maxMtime || lastMTime + MAX_REBUILD_OFFSET_IN_S > now) {
            logger.info("ContentTree seems to be outdated - Loading...")
            this.tree = buildContentTree()
            lastMTime = maxMtime
        }
    }

    private fun buildContentTree(): ContentTree {
        val tree = ContentTree()
        val root = ContentNode(0, -1, "ROOT")
        tree.addNode(root)
        nextcloudDB.mainNodes().forEach { n ->
            root.addNode(n)
            fillNode(n, tree)
        }
        logger.info("Getting content from group folders...")
        nextcloudDB.groupFolders().forEach { n ->
            logger.info(" Group folder found: {}", n.name)
            root.addNode(n)
            fillNode(n, tree)
        }
        logger.info("Found {} items in {} nodes", tree.itemsCount, tree.nodesCount)
        loadThumbnails(tree)
        return tree
    }

    private fun loadThumbnails(tree: ContentTree) {
        logger.info("Loading thumbnails...")
        val thumbsCount = AtomicInteger()
        nextcloudDB.processThumbnails { thumb ->
            getItemIdForThumbnail(thumb)?.let { id ->
                tree.getItem(id)?.let { item ->
                    item.thumb ?: let {
                        logger.debug("Adding thumbnail for item {}: {}", id, thumb)
                        item.thumb = thumb
                        tree.addItem(thumb)
                        thumbsCount.getAndIncrement()
                    }
                }
            }
        }
        logger.info("Found {} thumbnails", thumbsCount)
    }

    private fun getItemIdForThumbnail(thumb: ContentItem): String? {
        val pattern = Pattern.compile("^.*/preview(/[0-9a-f])+/(\\d+)/\\w+")
        val matcher = pattern.matcher(thumb.path)
        return if (matcher.find()) {
            matcher.group(matcher.groupCount())
        } else null
    }

    private fun fillNode(node: ContentNode, tree: ContentTree) {
        nextcloudDB.appendChildren(node)
        tree.addNode(node)
        node.items.forEach { item ->
            logger.debug("Adding item[{}]: " + item.path, item.id)
            tree.addItem(item)
        }
        node.nodes.forEach { n ->
            logger.debug("Adding node: " + n.name)
            fillNode(n, tree)
        }
    }

    fun getItem(id: String): ContentItem? = tree.getItem(id)
    fun getNode(id: String): ContentNode? = tree.getNode(id)

    companion object : KLogging() {
        const val REBUILD_TREE_DELAY_IN_MS = 1000 * 60L // 1m
        const val REBUILD_TREE_INIT_DELAY_IN_MS = 1000 * 60L // 1m
        const val MAX_REBUILD_OFFSET_IN_S = 60 * 60 * 12L // 12h
    }
}


class ContentTree {
    private val nodes: MutableMap<String, ContentNode> = HashMap()
    private val items: MutableMap<String, ContentItem> = HashMap()

    fun getNode(id: String): ContentNode? {
        return nodes[id]
    }

    fun getItem(id: String): ContentItem? {
        return items[id]
    }

    fun addItem(item: ContentItem) {
        items["${item.id}"] = item
    }

    fun addNode(node: ContentNode) {
        nodes["${node.id}"] = node
    }

    val itemsCount get() = items.size
    val nodesCount get() = nodes.size
}

