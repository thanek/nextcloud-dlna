package net.schowek.nextclouddlna.nextcloud.db

import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import net.schowek.nextclouddlna.nextcloud.content.Thumbnail
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.function.Consumer

@Component
class PreviewTableThumbnailProcessor(
    private val nextcloudConfig: NextcloudConfigDiscovery,
    private val previewRepository: PreviewRepository,
    private val mimetypeResolver: MimetypeResolver,
    jdbcTemplate: JdbcTemplate
) : ThumbnailProcessor {
    private val previewsTableExists: Boolean = try {
        jdbcTemplate.queryForList("SELECT 1 FROM oc_previews WHERE 1 = 0")
        logger.info("Found oc_previews table, previews stored by Nextcloud 33+ will be used")
        true
    } catch (e: DataAccessException) {
        logger.info("No oc_previews table, assuming Nextcloud older than 33")
        false
    }

    override fun process(thumbConsumer: Consumer<Thumbnail>) {
        if (!previewsTableExists) return
        previewRepository.findLocalUnversioned().use { previews ->
            previews.map { toThumbnail(it) }.forEach { thumb -> thumb?.let { thumbConsumer.accept(it) } }
        }
    }

    private fun toThumbnail(p: Preview): Thumbnail? {
        val format = mimetypeResolver.resolve(p.mimetypeId)
            ?.let { mime -> MediaFormat.values().firstOrNull { it.mime == mime } }
            ?: return null
        return Thumbnail(
            itemId = p.fileId.toInt(),
            path = previewPath(p, format),
            format = format,
            fileLength = p.size,
            width = p.width,
            height = p.height
        )
    }

    private fun previewPath(p: Preview, format: MediaFormat): String {
        val previewRoot = "${nextcloudConfig.nextcloudDir.absolutePath}/${nextcloudConfig.appDataDir}/preview"
        val shardDirs = md5Hex(p.fileId.toString()).take(SHARD_DEPTH).toList().joinToString("/")
        return "$previewRoot/$shardDirs/${p.fileId}/${previewFileName(p, format)}"
    }

    private fun previewFileName(p: Preview, format: MediaFormat): String {
        val crop = if (p.cropped) "-crop" else ""
        val max = if (p.max) "-max" else ""
        return "${p.width}-${p.height}$crop$max.${extensionOf(format)}"
    }

    private fun extensionOf(format: MediaFormat): String = when (format.mime) {
        MediaFormat.JPEG.mime -> "jpg"
        MediaFormat.GIF.mime -> "gif"
        MediaFormat.WEBP.mime -> "webp"
        else -> "png"
    }

    private fun md5Hex(value: String): String =
        MessageDigest.getInstance("MD5").digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }

    companion object : KLogging() {
        private const val SHARD_DEPTH = 7
    }
}
