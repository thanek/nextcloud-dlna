package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.content.Thumbnail
import java.util.function.Consumer

interface ThumbnailProcessor {
    fun process(thumbConsumer: Consumer<Thumbnail>)
}
