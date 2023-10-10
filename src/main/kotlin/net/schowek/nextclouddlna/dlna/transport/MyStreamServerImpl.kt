package net.schowek.nextclouddlna.dlna.transport

import org.jupnp.transport.Router
import org.jupnp.transport.spi.StreamServer
import java.net.InetAddress


class MyStreamServerImpl(
    private val configuration: MyStreamServerConfiguration
) : StreamServer<MyStreamServerConfiguration> {
    override fun init(bindAddress: InetAddress, router: Router) {
    }

    override fun getPort(): Int {
        return configuration.listenPort
    }

    override fun getConfiguration(): MyStreamServerConfiguration {
        return configuration
    }

    override fun run() {
    }

    override fun stop() {
    }
}

