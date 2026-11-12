package net.schowek.nextclouddlna.nextcloud.db

import jakarta.annotation.PostConstruct
import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import net.schowek.nextclouddlna.nextcloud.content.Thumbnail
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class FilecacheThumbnailProcessor(
    private val nextcloudConfig: NextcloudConfigDiscovery,
    private val filecacheRepository: FilecacheRepository,
    private val mimetypeResolver: MimetypeResolver,
    private val pathBuilder: PathBuilder
) : ThumbnailProcessor {
    private val thumbStorageId: Int = filecacheRepository.findFirstByPath(nextcloudConfig.appDataDir).storage

    @PostConstruct
    fun init() {
        logger.info("Using thumbnail storage id: {}", thumbStorageId)
    }

    override fun process(thumbConsumer: Consumer<Thumbnail>) {
        filecacheRepository.findThumbnails(
            "${nextcloudConfig.appDataDir}/preview/%",
            thumbStorageId,
            mimetypeResolver.folderMimeTypeId
        ).use { files ->
            files.map { toThumbnail(it) }.forEach { thumb -> thumb?.let { thumbConsumer.accept(it) } }
        }
    }

    private fun toThumbnail(f: Filecache): Thumbnail? {
        val itemId = PREVIEW_DIR_PATTERN.find(f.path)?.groupValues?.get(1)?.toInt() ?: return null
        val dimensions = PREVIEW_NAME_PATTERN.find(f.name) ?: return null
        val format = mimetypeResolver.resolve(f.mimetype)
            ?.let { mime -> MediaFormat.values().firstOrNull { it.mime == mime } }
            ?: return null
        return Thumbnail(
            itemId = itemId,
            path = pathBuilder.build(f),
            format = format,
            fileLength = f.size,
            width = dimensions.groupValues[1].toInt(),
            height = dimensions.groupValues[2].toInt()
        )
    }

    companion object : KLogging() {
        private val PREVIEW_DIR_PATTERN = Regex("/preview(?:/[0-9a-f])+/(\\d+)/[^/]+$")
        private val PREVIEW_NAME_PATTERN = Regex("(\\d+)-(\\d+)(?:-(?:crop|max))*\\.\\w+$")
    }
}
