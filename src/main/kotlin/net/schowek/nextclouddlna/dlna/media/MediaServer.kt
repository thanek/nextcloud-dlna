package net.schowek.nextclouddlna.dlna.media

import jakarta.annotation.PostConstruct
import net.schowek.nextclouddlna.util.ExternalUrls
import net.schowek.nextclouddlna.util.Logging
import org.jupnp.model.meta.*
import org.jupnp.model.types.UDADeviceType
import org.jupnp.model.types.UDN.uniqueSystemIdentifier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.IOException


@Component
class MediaServer(
    @Qualifier("contentDirectoryLocalService")
    private val contentDirectoryService: LocalService<*>,
    @Qualifier("connectionManagerLocalService")
    private val connectionManagerService: LocalService<*>,
    @Value("\${server.friendlyName}")
    private val friendlyName: String,
    private val externalUrls: ExternalUrls
) : Logging {
    val device = LocalDevice(
        DeviceIdentity(uniqueSystemIdentifier("DLNAtoad-MediaServer"), 300),
        UDADeviceType(DEVICE_TYPE, VERSION),
        DeviceDetails(friendlyName, externalUrls.selfURI),
        createDeviceIcon(), arrayOf(contentDirectoryService, connectionManagerService)
    )

    @PostConstruct
    fun init() {
        logger.info("uniqueSystemIdentifier: {} ({})", device.identity.udn, friendlyName)
    }

    companion object {
        private const val DEVICE_TYPE = "MediaServer"
        private const val VERSION = 1
        const val ICON_FILENAME = "icon.png"

        @Throws(IOException::class)
        fun createDeviceIcon(): Icon {
            val resource = MediaServer::class.java.getResourceAsStream("/$ICON_FILENAME")
                ?: throw IllegalStateException("Icon not found.")
            return resource.use { res ->
                Icon("image/png", 48, 48, 8, ICON_FILENAME, res).also {
                    it.validate()
                }
            }
        }
    }
}

