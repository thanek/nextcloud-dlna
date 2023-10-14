package net.schowek.nextclouddlna.nextcloud.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.File

interface NextcloudAppPathProvider {
    val nextcloudDir: File
}

@Component
@Profile("!integration")
class NextcloudAppPathsProviderImpl(
    @Value("\${nextcloud.filesDir}")
    private val dirPath: String,
) : NextcloudAppPathProvider {
    override val nextcloudDir = ensureNextcloudDir(dirPath)

    private fun ensureNextcloudDir(dirPath: String): File {
        if (dirPath.isEmpty()) {
            throw RuntimeException("No nextcloud data directory name provided")
        }
        return File(dirPath).also {
            if (!it.exists() || !it.isDirectory) {
                throw RuntimeException("Invalid nextcloud data directory specified")
            }
        }
    }
}