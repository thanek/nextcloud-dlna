package net.schowek.nextclouddlna.dlna

import net.schowek.nextclouddlna.dlna.transport.ApacheStreamClient
import net.schowek.nextclouddlna.dlna.transport.ApacheStreamClientConfiguration
import net.schowek.nextclouddlna.dlna.transport.MyStreamServerConfiguration
import net.schowek.nextclouddlna.dlna.transport.MyStreamServerImpl
import org.jupnp.DefaultUpnpServiceConfiguration
import org.jupnp.transport.spi.NetworkAddressFactory
import org.jupnp.transport.spi.StreamClient
import org.jupnp.transport.spi.StreamServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MyUpnpServiceConfiguration(
    @Value("\${server.port}")
    streamListenPort: Int
) : DefaultUpnpServiceConfiguration(streamListenPort) {
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
}
