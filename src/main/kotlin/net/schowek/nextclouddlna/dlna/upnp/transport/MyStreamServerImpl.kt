package net.schowek.nextclouddlna.dlna.upnp.transport

import mu.KLogging
import org.jupnp.transport.Router
import org.jupnp.transport.spi.StreamServer
import java.net.InetAddress


class MyStreamServerImpl(
    private val configuration: MyStreamServerConfiguration
) : StreamServer<MyStreamServerConfiguration> {
    override fun init(bindAddress: InetAddress, router: Router) {}

    override fun getPort() = configuration.listenPort

    override fun getConfiguration() = configuration

    override fun run() {}

    override fun stop() {}

    companion object : KLogging()
}

