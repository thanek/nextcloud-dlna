package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import org.springframework.stereotype.Component

@Component
class DefaultContentItemFactory(
    private val mimetypeResolver: MimetypeResolver
) : ContentItemFactory {
    override fun create(filecache: Filecache, path: String): ContentItem {
        return try {
            val format = MediaFormat.fromMimeType(mimetypeResolver.resolve(filecache.mimetype)
                ?: throw IllegalStateException("Unknown mimetype id: ${filecache.mimetype} for file: ${filecache.path}"))
            ContentItem(filecache.id, filecache.parent, filecache.name, path, format, filecache.size, filecache.mtime)
        } catch (e: Exception) {
            throw RuntimeException("Unable to create ContentItem for ${filecache.path}: ${e.message}", e)
        }
    }
}
