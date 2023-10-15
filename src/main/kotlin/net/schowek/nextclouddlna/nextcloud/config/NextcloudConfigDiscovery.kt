package net.schowek.nextclouddlna.nextcloud.config

import mu.KLogging
import net.schowek.nextclouddlna.nextcloud.db.AppConfigRepository
import org.springframework.stereotype.Component
import java.io.File
import java.util.Arrays.*
import java.util.Objects.*


@Component
class NextcloudConfigDiscovery(
    val appConfigRepository: AppConfigRepository,
    val nextcloudDirProvider: NextcloudAppPathProvider
) {
    val appDataDir: String = findAppDataDir()
    val nextcloudDir: File get() = nextcloudDirProvider.nextcloudDir
    val supportsGroupFolders: Boolean get() = checkGroupFoldersSupport()

    fun checkGroupFoldersSupport(): Boolean {
        return "yes" == appConfigRepository.getValue("groupfolders", "enabled")
    }

    private fun findAppDataDir(): String {
        return stream(requireNonNull(nextcloudDir.listFiles { f ->
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
