package net.schowek.nextclouddlna.nextcloud.db

import jakarta.annotation.PostConstruct
import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class DefaultThumbnailProcessor(
    private val nextcloudConfig: NextcloudConfigDiscovery,
    private val filecacheRepository: FilecacheRepository,
    private val mimetypeResolver: MimetypeResolver,
    private val contentItemFactory: ContentItemFactory,
    private val pathBuilder: PathBuilder
) : ThumbnailProcessor {
    private val thumbStorageId: Int = filecacheRepository.findFirstByPath(nextcloudConfig.appDataDir).storage

    @PostConstruct
    fun init() {
        logger.info("Using thumbnail storage id: {}", thumbStorageId)
    }

    override fun process(thumbConsumer: Consumer<ContentItem>) {
        filecacheRepository.findThumbnails(
            "${nextcloudConfig.appDataDir}/preview/%",
            thumbStorageId,
            mimetypeResolver.folderMimeTypeId
        ).use { files ->
            files.map { f: Filecache -> contentItemFactory.create(f, pathBuilder.build(f)) }.forEach(thumbConsumer)
        }
    }

    companion object : KLogging()
}
