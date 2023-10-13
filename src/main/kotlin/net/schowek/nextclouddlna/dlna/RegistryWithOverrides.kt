package net.schowek.nextclouddlna.dlna

import mu.KLogging
import net.schowek.nextclouddlna.dlna.media.MediaServer
import net.schowek.nextclouddlna.dlna.media.MediaServer.Companion.ICON_FILENAME
import org.jupnp.UpnpService
import org.jupnp.model.resource.IconResource
import org.jupnp.model.resource.Resource
import org.jupnp.registry.RegistryImpl
import org.jupnp.registry.RegistryMaintainer
import java.io.IOException
import java.net.URI


class RegistryImplWithOverrides(
    private val upnpService: UpnpService
) : RegistryImpl(upnpService) {
    private var icon: Resource<*>

    init {
        try {
            val deviceIcon = MediaServer.createDeviceIcon()
            icon = IconResource(deviceIcon.uri, deviceIcon)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Synchronized
    @Throws(IllegalArgumentException::class)
    override fun getResource(pathQuery: URI): Resource<*>? {
        return if ("/$ICON_FILENAME" == pathQuery.path) {
            icon
        } else super.getResource(pathQuery)
    }

    companion object : KLogging()
}
