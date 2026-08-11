package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import java.util.function.Consumer

interface ThumbnailProcessor {
    fun process(thumbConsumer: Consumer<ContentItem>)
}
