package net.schowek.nextclouddlna.dlna

import mu.KLogging
import org.jupnp.UpnpService
import org.jupnp.model.meta.LocalDevice
import org.jupnp.protocol.ProtocolFactoryImpl
import org.jupnp.protocol.async.SendingNotificationAlive

class MyProtocolFactory(
    upnpService: UpnpService
) : ProtocolFactoryImpl(upnpService) {
    override fun createSendingNotificationAlive(localDevice: LocalDevice): SendingNotificationAlive {
        logger.info { "SENDING ALIVE $localDevice" }
        return SendingNotificationAlive(upnpService, localDevice)
    }
    companion object : KLogging()
}