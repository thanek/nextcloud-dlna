package net.schowek.nextclouddlna.nextcloud

import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import net.schowek.nextclouddlna.nextcloud.db.*
import net.schowek.nextclouddlna.nextcloud.db.Filecache.Companion.FOLDER_MIME_TYPE
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.InvalidDataAccessResourceUsageException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.collections.HashMap


@Component
class MediaDB(
    nextcloudConfig: NextcloudConfigDiscovery,
    mimetypeRepository: MimetypeRepository,
    filecacheRepository: FilecacheRepository,
    groupFolderRepository: GroupFolderRepository
) {
    final var logger: Logger = LoggerFactory.getLogger(MediaDB::class.java)

    private val appdataDir: String
    private val thumbStorageId: Int
    private val contentDir: String
    private val supportsGroupFolders: Boolean
    private val mimetypeRepository: MimetypeRepository
    private val filecacheRepository: FilecacheRepository
    private val groupFolderRepository: GroupFolderRepository
    private val storageUsersMap: MutableMap<Int, String> = HashMap()
    private val mimetypes: Map<Int, String>
    private val folderMimeType: Int

    init {
        appdataDir = nextcloudConfig.appDataDir
        contentDir = nextcloudConfig.nextcloudDir
        supportsGroupFolders = nextcloudConfig.supportsGroupFolders
        this.mimetypeRepository = mimetypeRepository
        this.filecacheRepository = filecacheRepository
        this.groupFolderRepository = groupFolderRepository
        thumbStorageId = filecacheRepository.findFirstByPath(appdataDir).storage
        logger.info("Using thumbnail storage id: {}", thumbStorageId)
        mimetypes = mimetypeRepository.findAll().associate { it.id to it.mimetype }
        folderMimeType = mimetypes.entries.find { it.value == FOLDER_MIME_TYPE }!!.key
    }

    @Transactional(readOnly = true)
    fun processThumbnails(thumbConsumer: Consumer<ContentItem>) {
        filecacheRepository.findThumbnails("$appdataDir/preview/%", thumbStorageId, folderMimeType).use { files ->
            files.map { f: Filecache -> asItem(f) }.forEach(thumbConsumer)
        }
    }

    fun mainNodes(): List<ContentNode> =
        filecacheRepository.mainNodes().map { o -> asNode(o[0] as Filecache, o[1] as Mount) }.toList()

    fun groupFolders(): List<ContentNode> {
        when {
            supportsGroupFolders -> {
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
        val format = MediaFormat.fromMimeType(mimetypes[f.mimetype]!!)
        val path: String = buildPath(f)
        return ContentItem(f.id, f.parent, f.name, path, format, f.size)
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
            contentDir + "/" + userName + "/" + f.path
        } else {
            contentDir + "/" + f.path
        }
    }
}

