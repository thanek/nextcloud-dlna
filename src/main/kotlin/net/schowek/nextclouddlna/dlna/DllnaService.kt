package net.schowek.nextclouddlna.dlna

import mu.KLogging
import net.schowek.nextclouddlna.dlna.media.MediaServer
import net.schowek.nextclouddlna.dlna.transport.ApacheStreamClient
import net.schowek.nextclouddlna.dlna.transport.ApacheStreamClientConfiguration
import net.schowek.nextclouddlna.dlna.transport.MyStreamServerConfiguration
import net.schowek.nextclouddlna.dlna.transport.MyStreamServerImpl
import net.schowek.nextclouddlna.util.ServerInfoProvider
import org.jupnp.DefaultUpnpServiceConfiguration
import org.jupnp.UpnpService
import org.jupnp.UpnpServiceConfiguration
import org.jupnp.UpnpServiceImpl
import org.jupnp.model.meta.LocalDevice
import org.jupnp.protocol.ProtocolFactory
import org.jupnp.protocol.ProtocolFactoryImpl
import org.jupnp.protocol.async.SendingNotificationAlive
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
    private val mediaServer: MediaServer
) {
    // Named this way cos NetworkAddressFactoryImpl has a bindAddresses field.
    private val addressesToBind: List<InetAddress> = listOf(serverInfoProvider.address!!)

    fun start() = MyUpnpService(MyUpnpServiceConfiguration()).also {
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

    private inner class MyUpnpServiceConfiguration : DefaultUpnpServiceConfiguration(8080) {
        override fun createStreamClient(): StreamClient<*> {
            return ApacheStreamClient(
                ApacheStreamClientConfiguration(syncProtocolExecutorService)
            )
        }

        override fun createStreamServer(networkAddressFactory: NetworkAddressFactory): StreamServer<*> {
            return MyStreamServerImpl(
                MyStreamServerConfiguration(networkAddressFactory.streamListenPort)
            )
        }

        override fun createNetworkAddressFactory(
            streamListenPort: Int,
            multicastResponsePort: Int
        ): NetworkAddressFactory {
            return MyNetworkAddressFactory(streamListenPort, multicastResponsePort)
        }
    }

    inner class MyNetworkAddressFactory(
        streamListenPort: Int,
        multicastResponsePort: Int
    ) : NetworkAddressFactoryImpl(streamListenPort, multicastResponsePort) {
        override fun isUsableAddress(iface: NetworkInterface, address: InetAddress): Boolean {
            return addressesToBind.contains(address)
        }
    }
}


class MyProtocolFactory(
    upnpService: UpnpService
) : ProtocolFactoryImpl(upnpService) {
    override fun createSendingNotificationAlive(localDevice: LocalDevice): SendingNotificationAlive {
        logger.info { "SENDING ALIVE $localDevice" }
        return SendingNotificationAlive(upnpService, localDevice)
    }

    companion object : KLogging()
}