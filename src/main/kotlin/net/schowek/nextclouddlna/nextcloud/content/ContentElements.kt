package net.schowek.nextclouddlna.nextcloud.content

class ContentItem(
    val id: Int,
    val parentId: Int,
    val name: String,
    val path: String,
    val format: MediaFormat,
    val fileLength: Long
) {
    var thumb: ContentItem? = null
}

class ContentNode(
    val id: Int,
    val parentId: Int,
    val name: String
) {
    private val nodes: MutableList<ContentNode> = ArrayList()
    private val items: MutableList<ContentItem> = ArrayList()


    private val nodeCount: Int get() = nodes.size
    private val itemCount: Int get() = items.size
    val nodeAndItemCount: Int get() = nodeCount + itemCount

    fun addItem(item: ContentItem) {
        items.add(item)
    }

    fun addNode(node: ContentNode) {
        nodes.add(node)
    }

    fun getItems(): List<ContentItem> {
        return items
    }

    fun getNodes(): List<ContentNode> {
        return nodes
    }

}
