package net.schowek.nextclouddlna.dlna.upnp

import net.schowek.nextclouddlna.dlna.upnp.transport.ApacheStreamClient
import net.schowek.nextclouddlna.dlna.upnp.transport.ApacheStreamClientConfiguration
import net.schowek.nextclouddlna.dlna.upnp.transport.MyStreamServerConfiguration
import net.schowek.nextclouddlna.dlna.upnp.transport.MyStreamServerImpl
import net.schowek.nextclouddlna.util.ServerInfoProvider
import org.jupnp.DefaultUpnpServiceConfiguration
import org.jupnp.transport.impl.NetworkAddressFactoryImpl
import org.jupnp.transport.spi.DatagramIO
import org.jupnp.transport.spi.NetworkAddressFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.NetworkInterface

@Component
@Profile("!integration")
class MyUpnpServiceConfiguration(
    private val serverInfoProvider: ServerInfoProvider
) : DefaultUpnpServiceConfiguration(serverInfoProvider.port) {
    val addressesToBind = listOf(serverInfoProvider.host)

    override fun createStreamClient() =
        ApacheStreamClient(ApacheStreamClientConfiguration(syncProtocolExecutorService))

    override fun createStreamServer(networkAddressFactory: NetworkAddressFactory) =
        MyStreamServerImpl(MyStreamServerConfiguration(networkAddressFactory.streamListenPort))

    override fun createDatagramIO(networkAddressFactory: NetworkAddressFactory): DatagramIO<*> {
        return super.createDatagramIO(networkAddressFactory)
    }

    override fun createNetworkAddressFactory(streamListenPort: Int, multicastResponsePort: Int) =
        MyNetworkAddressFactory(serverInfoProvider, multicastResponsePort)

    inner class MyNetworkAddressFactory(
        private val serverInfoProvider: ServerInfoProvider,
        multicastResponsePort: Int
    ) : NetworkAddressFactoryImpl(serverInfoProvider.port, multicastResponsePort) {
        override fun isUsableAddress(iface: NetworkInterface, address: InetAddress) =
            addressesToBind.contains(address.hostAddress)
    }
}
