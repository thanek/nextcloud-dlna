package net.schowek.nextclouddlna.nextcloud.db

import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import org.springframework.dao.InvalidDataAccessResourceUsageException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer


@Component
class NextcloudDB(
    private val nextcloudConfig: NextcloudConfigDiscovery,
    private val filecacheRepository: FilecacheRepository,
    private val groupFolderRepository: GroupFolderRepository,
    private val mimetypeResolver: MimetypeResolver,
    private val storageUserMapper: StorageUserMapper,
    private val pathBuilder: PathBuilder,
    private val contentItemFactory: ContentItemFactory,
    private val thumbnailProcessor: ThumbnailProcessor
) {
    @Transactional(readOnly = true)
    fun processThumbnails(thumbConsumer: Consumer<ContentItem>) = thumbnailProcessor.process(thumbConsumer)

    fun mainNodes(): List<ContentNode> =
        filecacheRepository.mainNodes().map { o ->
            val filecache = o[0] as Filecache
            val mount = o[1] as Mount
            storageUserMapper.map(filecache.storage, mount.userId)
            ContentNode(filecache.id, filecache.parent, mount.userId)
        }.toList()

    fun groupFolders(): List<ContentNode> {
        when {
            nextcloudConfig.supportsGroupFolders -> {
                try {
                    return groupFolderRepository.findAll().flatMap { g ->
                        filecacheRepository.findByPath("__groupfolders/" + g.id).map { f ->
                            ContentNode(f.id, f.parent, g.name)
                        }.toList()
                    }
                } catch (e: InvalidDataAccessResourceUsageException) {
                    logger.warn(e.message)
                }
            }
        }
        return emptyList()
    }

    fun appendChildren(n: ContentNode) {
        val children = filecacheRepository.findByParent(n.id)
        val folderMimeTypeId = mimetypeResolver.folderMimeTypeId

        children.filter { f -> f.mimetype == folderMimeTypeId }
            .forEach { folder -> n.addNode(ContentNode(folder.id, folder.parent, folder.name)) }

        children.filter { f -> f.mimetype != folderMimeTypeId }
            .forEach { file ->
                runCatching {
                    n.addItem(contentItemFactory.create(file, pathBuilder.build(file)))
                }.recover {
                    logger.warn(it.message)
                }
            }
    }

    fun maxMtime(): Long = filecacheRepository.findFirstByOrderByStorageMtimeDesc().storageMtime

    companion object : KLogging()
}
