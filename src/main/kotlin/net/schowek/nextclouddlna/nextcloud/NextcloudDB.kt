package net.schowek.nextclouddlna.nextcloud

import jakarta.annotation.PostConstruct
import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import net.schowek.nextclouddlna.nextcloud.db.*
import net.schowek.nextclouddlna.nextcloud.db.Filecache.Companion.FOLDER_MIME_TYPE
import org.springframework.dao.InvalidDataAccessResourceUsageException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer


@Component
class NextcloudDB(
    private val nextcloudConfig: NextcloudConfigDiscovery,
    private val mimetypeRepository: MimetypeRepository,
    private val filecacheRepository: FilecacheRepository,
    private val groupFolderRepository: GroupFolderRepository
) {
    private val thumbStorageId: Int = filecacheRepository.findFirstByPath(nextcloudConfig.appDataDir).storage
    private val mimetypes: Map<Int, String> = mimetypeRepository.findAll().associate { it.id to it.mimetype }
    private val folderMimeType: Int = mimetypes.entries.find { it.value == FOLDER_MIME_TYPE }!!.key
    private val storageUsersMap: MutableMap<Int, String> = HashMap()

    @PostConstruct
    fun init() {
        logger.info("Using thumbnail storage id: {}", thumbStorageId)
    }

    @Transactional(readOnly = true)
    fun processThumbnails(thumbConsumer: Consumer<ContentItem>) {
        filecacheRepository.findThumbnails("${nextcloudConfig.appDataDir}/preview/%", thumbStorageId, folderMimeType)
            .use { files ->
                files.map { f: Filecache -> asItem(f) }.forEach(thumbConsumer)
            }
    }

    fun mainNodes(): List<ContentNode> =
        filecacheRepository.mainNodes().map { o -> asNode(o[0] as Filecache, o[1] as Mount) }.toList()

    fun groupFolders(): List<ContentNode> {
        when {
            nextcloudConfig.supportsGroupFolders -> {
                try {
                    return groupFolderRepository.findAll().flatMap { g ->
                        filecacheRepository.findByPath("__groupfolders/" + g.id).map { f ->
                            asNode(f, g)
                        }.toList()
                    }
                } catch (e: InvalidDataAccessResourceUsageException) {
                    logger.warn(e.message)
                }
            }
        }
        return emptyList()
    }

    private fun asItem(f: Filecache): ContentItem {
        try {
            val format = MediaFormat.fromMimeType(mimetypes[f.mimetype]!!)
            val path: String = buildPath(f)
            return ContentItem(f.id, f.parent, f.name, path, format, f.size)
        } catch (e: Exception) {
            throw RuntimeException("Unable to create ContentItem for ${f.path}: ${e.message}")
        }
    }

    private fun asNode(f: Filecache): ContentNode {
        return ContentNode(f.id, f.parent, f.name)
    }

    private fun asNode(f: Filecache, m: Mount): ContentNode {
        storageUsersMap[f.storage] = m.userId
        return ContentNode(f.id, f.parent, m.userId)
    }

    private fun asNode(f: Filecache, g: GroupFolder): ContentNode {
        return ContentNode(f.id, f.parent, g.name)
    }

    fun appendChildren(n: ContentNode) {
        val children = filecacheRepository.findByParent(n.id)

        children.filter { f -> f.mimetype == folderMimeType }
            .forEach { folder -> n.addNode(asNode(folder)) }

        try {
            children.filter { f -> f.mimetype != folderMimeType }
                .forEach { file -> n.addItem(asItem(file)) }
        } catch (e: Exception) {
            logger.warn(e.message)
        }
    }

    fun maxMtime(): Long = filecacheRepository.findFirstByOrderByStorageMtimeDesc().storageMtime

    private fun buildPath(f: Filecache): String {
        return if (storageUsersMap.containsKey(f.storage)) {
            val userName: String? = storageUsersMap[f.storage]
            "${nextcloudConfig.nextcloudDir.absolutePath}/$userName/${f.path}"
        } else {
            "${nextcloudConfig.nextcloudDir.absolutePath}/${f.path}"
        }
    }

    companion object : KLogging()
}

