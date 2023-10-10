package net.schowek.nextclouddlna.nextcloud

import jakarta.annotation.PostConstruct
import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.db.AppConfigRepository
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
    final val appDataDir: String = findAppDataDir()
    final val supportsGroupFolders: Boolean = checkGroupFoldersSupport()

    private fun checkGroupFoldersSupport(): Boolean {
        return "yes" == appConfigRepository.getValue("groupfolders", "enabled")
    }

    private fun findAppDataDir(): String {
        return stream(requireNonNull(File(nextcloudDir).listFiles { f ->
            f.isDirectory && f.name.matches(APPDATA_NAME_PATTERN.toRegex())
        })).findFirst().orElseThrow().name
            .also {
                logger.info { "Found appdata dir: $it" }
            }
    }

    companion object : KLogging() {
        const val APPDATA_NAME_PATTERN = "appdata_\\w+"
    }
}
