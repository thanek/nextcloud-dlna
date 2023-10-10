package net.schowek.nextclouddlna.nextcloud.content

import jakarta.annotation.PostConstruct
import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.MediaDB
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern


@Component
class ContentTreeProvider(
    private val mediaDB: MediaDB
) {
    private var tree = buildContentTree()
    private var lastMTime = 0L

    init {
        rebuildTree()
    }

    @PostConstruct
    @Scheduled(fixedDelay = 1000 * 60, initialDelay = 1000 * 60)
    final fun rebuildTree() {
        val maxMtime: Long = mediaDB.maxMtime()
        if (lastMTime < maxMtime) {
            logger.info("ContentTree seems to be outdated - Loading...")
            this.tree = buildContentTree()
            lastMTime = maxMtime
        }
    }

    private fun buildContentTree(): ContentTree {
        val tree = ContentTree()
        val root = ContentNode(0, -1, "ROOT")
        tree.addNode(root)
        mediaDB.mainNodes().forEach { n ->
            root.addNode(n)
            fillNode(n, tree)
        }
        logger.info("Getting content from group folders...")
        mediaDB.groupFolders().forEach { n ->
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
        mediaDB.processThumbnails { thumb ->
            val id = getItemIdForThumbnail(thumb)
            if (id != null) {
                val item = tree.getItem(id)
                if (item != null && item.thumb == null) {
                    logger.debug("Adding thumbnail for item {}: {}", id, thumb)
                    item.thumb = thumb
                    tree.addItem(thumb)
                    thumbsCount.getAndIncrement()
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
        mediaDB.appendChildren(node)
        tree.addNode(node)
        node.getItems().forEach { item ->
            logger.debug("Adding item[{}]: " + item.path, item.id)
            tree.addItem(item)
        }
        node.getNodes().forEach { n ->
            logger.debug("Adding node: " + n.name)
            fillNode(n, tree)
        }
    }

    fun getItem(id: String): ContentItem? = tree.getItem(id)
    fun getNode(id: String): ContentNode? = tree.getNode(id)

    companion object : KLogging()
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

