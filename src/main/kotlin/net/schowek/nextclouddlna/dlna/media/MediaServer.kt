package net.schowek.nextclouddlna.dlna.media

import jakarta.annotation.PostConstruct
import mu.KLogging
import net.schowek.nextclouddlna.util.ExternalUrls
import org.jupnp.model.meta.*
import org.jupnp.model.types.UDADeviceType
import org.jupnp.model.types.UDN.uniqueSystemIdentifier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream


@Component
class MediaServer(
    @Qualifier("contentDirectoryLocalService")
    private val contentDirectoryService: LocalService<*>,
    @Qualifier("connectionManagerLocalService")
    private val connectionManagerService: LocalService<*>,
    @Value("\${server.friendlyName}")
    private val friendlyName: String,
    externalUrls: ExternalUrls
) {
    final val device = LocalDevice(
        DeviceIdentity(uniqueSystemIdentifier("Nextcloud-DLNA-MediaServer"), 300),
        UDADeviceType(DEVICE_TYPE, VERSION),
        DeviceDetails(friendlyName, externalUrls.selfURI),
        createDeviceIcon(),
        arrayOf(contentDirectoryService, connectionManagerService)
    )

    init {
        logger.info("uniqueSystemIdentifier: {} ({})", device.identity.udn, friendlyName)
    }

    companion object : KLogging() {
        private const val DEVICE_TYPE = "MediaServer"
        private const val VERSION = 1
        const val ICON_FILENAME = "icon.png"

        @Throws(IOException::class)
        fun createDeviceIcon(): Icon {
            val resource = iconResource()
            return resource.use { res ->
                Icon("image/png", 48, 48, 8, ICON_FILENAME, res).also {
                    it.validate()
                }
            }
        }

        fun iconResource(): InputStream {
            return MediaServer::class.java.getResourceAsStream("/$ICON_FILENAME")
                ?: throw IllegalStateException("Icon not found.")
        }
    }
}

