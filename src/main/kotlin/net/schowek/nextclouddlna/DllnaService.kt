package net.schowek.nextclouddlna

import jakarta.annotation.PreDestroy
import mu.KLogging
import net.schowek.nextclouddlna.dlna.RegistryImplWithOverrides
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
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.NetworkInterface


@Component
class DlnaService(
    private val mediaServer: MediaServer,
    private val serverInfoProvider: ServerInfoProvider,
) {
    private val addressesToBind: List<InetAddress> = listOf(serverInfoProvider.address!!)
    var upnpService: UpnpService = MyUpnpService(MyUpnpServiceConfiguration())

    fun start() {
        upnpService.startup()
        upnpService.registry.addDevice(mediaServer.device)
    }

    @EventListener
    fun handleContextRefresh(event: ContextRefreshedEvent) {
        start()
    }

    @PreDestroy
    fun destroy() {
        upnpService.shutdown()
    }

    inner class MyUpnpService(
        configuration: UpnpServiceConfiguration
    ) : UpnpServiceImpl(configuration) {
        init {
            protocolFactory = createProtocolFactory()
        }

        override fun createRegistry(pf: ProtocolFactory): Registry {
            return RegistryImplWithOverrides(this)
        }
    }

    private inner class MyUpnpServiceConfiguration : DefaultUpnpServiceConfiguration(serverInfoProvider.port) {
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

    companion object : KLogging()
}
