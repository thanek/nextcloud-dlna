package net.schowek.nextclouddlna.nextcloud

import net.schowek.nextclouddlna.nextcloud.db.AppConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import java.util.Arrays.*
import java.util.Objects.*


@Component
class NextcloudConfigDiscovery(
    @Value("\${nextcloud.filesDir}")
    val nextcloudDir: String,
    val appConfigRepository: AppConfigRepository
) {
    final var logger = LoggerFactory.getLogger(NextcloudConfigDiscovery::class.java)

    final val appDataDir: String
    final val supportsGroupFolders: Boolean

    init {
        appDataDir = findAppDataDir()
        supportsGroupFolders = checkGroupFoldersSupport()
        logger.info("Found appdata dir: {}", appDataDir)
    }

    private fun checkGroupFoldersSupport(): Boolean {
        return "yes" == appConfigRepository.getValue("groupfolders", "enabled")
    }

    private fun findAppDataDir(): String {
        return stream(requireNonNull(File(nextcloudDir).listFiles { f ->
            f.isDirectory && f.name.matches(APPDATA_NAME_PATTERN.toRegex())
        })).findFirst().orElseThrow().name
    }

    companion object {
        const val APPDATA_NAME_PATTERN = "appdata_\\w+"
    }
}
