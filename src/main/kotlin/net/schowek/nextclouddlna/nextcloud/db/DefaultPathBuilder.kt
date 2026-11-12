package net.schowek.nextclouddlna.nextcloud.db

import net.schowek.nextclouddlna.nextcloud.config.NextcloudConfigDiscovery
import org.springframework.stereotype.Component

@Component
class DefaultPathBuilder(
    private val nextcloudConfig: NextcloudConfigDiscovery,
    private val storageUserMapper: StorageUserMapper
) : PathBuilder {
    override fun build(filecache: Filecache): String {
        val baseDir = nextcloudConfig.nextcloudDir.absolutePath
        return storageUserMapper.getUsername(filecache.storage)
            ?.let { userName -> "$baseDir/$userName/${filecache.path}" }
            ?: "$baseDir/${filecache.path}"
    }
}
