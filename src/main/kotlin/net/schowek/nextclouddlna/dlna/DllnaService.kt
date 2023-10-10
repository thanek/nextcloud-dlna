package net.schowek.nextclouddlna.dlna

import net.schowek.nextclouddlna.dlna.media.MediaServer
import net.schowek.nextclouddlna.dlna.transport.ApacheStreamClient
import net.schowek.nextclouddlna.dlna.transport.ApacheStreamClientConfiguration
import net.schowek.nextclouddlna.dlna.transport.MyStreamServerConfiguration
import net.schowek.nextclouddlna.dlna.transport.MyStreamServerImpl
import net.schowek.nextclouddlna.util.ServerInfoProvider
import org.jupnp.DefaultUpnpServiceConfiguration
import org.jupnp.UpnpServiceConfiguration
import org.jupnp.UpnpServiceImpl
import org.jupnp.protocol.ProtocolFactory
import org.jupnp.protocol.ProtocolFactoryImpl
import org.jupnp.registry.Registry
import org.jupnp.transport.impl.NetworkAddressFactoryImpl
import org.jupnp.transport.spi.NetworkAddressFactory
import org.jupnp.transport.spi.StreamClient
import org.jupnp.transport.spi.StreamServer
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.NetworkInterface


@Component
class DlnaService(
    private val serverInfoProvider: ServerInfoProvider,
    private val upnpServiceConfiguration: MyUpnpServiceConfiguration,
    private val mediaServer: MediaServer
) {
    fun start() = MyUpnpService(upnpServiceConfiguration).also {
        it.startup()
        it.registry.addDevice(mediaServer.device)
    }

    inner class MyUpnpService(
        configuration: UpnpServiceConfiguration
    ) : UpnpServiceImpl(configuration) {
        override fun createRegistry(pf: ProtocolFactory): Registry {
            return RegistryImplWithOverrides(this)
        }
        override fun createProtocolFactory(): ProtocolFactory? {
            return MyProtocolFactory(this)
        }
    }
}

