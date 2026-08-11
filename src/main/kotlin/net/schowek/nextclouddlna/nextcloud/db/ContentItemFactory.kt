package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.content.ContentItem

interface ContentItemFactory {
    fun create(filecache: Filecache, path: String): ContentItem
}
