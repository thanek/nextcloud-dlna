package net.schowek.nextclouddlna.nextcloud.content

import kotlin.math.max

data class ContentItem(
    val id: Int,
    val parentId: Int,
    val name: String,
    val path: String,
    val format: MediaFormat,
    val fileLength: Long,
    val mtime: Long = 0L
) {
    var thumb: Thumbnail? = null

    fun offerThumbnail(candidate: Thumbnail) {
        if (candidate.isDlnaCompatible && candidate.isBetterThan(thumb)) {
            thumb = candidate
        }
    }
}

data class Thumbnail(
    val itemId: Int,
    val path: String,
    val format: MediaFormat,
    val fileLength: Long,
    val width: Int,
    val height: Int
) {
    private val longestEdge: Int get() = max(width, height)
    private val coversTargetSize: Boolean get() = longestEdge >= TARGET_SIZE

    val isDlnaCompatible: Boolean get() = format.mime in DLNA_THUMBNAIL_MIMETYPES

    fun isBetterThan(other: Thumbnail?): Boolean = when {
        other == null -> true
        coversTargetSize != other.coversTargetSize -> coversTargetSize
        coversTargetSize -> longestEdge < other.longestEdge
        else -> longestEdge > other.longestEdge
    }

    companion object {
        const val TARGET_SIZE = 160
        private val DLNA_THUMBNAIL_MIMETYPES = setOf(MediaFormat.JPEG.mime, MediaFormat.PNG.mime)
    }
}

data class ContentNode(
    val id: Int,
    val parentId: Int,
    val name: String
) {
    val nodes: MutableList<ContentNode> = ArrayList()
    val items: MutableList<ContentItem> = ArrayList()

    private val nodeCount: Int get() = nodes.size
    private val itemCount: Int get() = items.size
    val nodeAndItemCount: Int get() = nodeCount + itemCount

    fun addItem(item: ContentItem) {
        items.add(item)
    }

    fun addNode(node: ContentNode) {
        nodes.add(node)
    }
}
