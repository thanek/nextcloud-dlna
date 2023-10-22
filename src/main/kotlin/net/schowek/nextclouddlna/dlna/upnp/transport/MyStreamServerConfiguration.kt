package net.schowek.nextclouddlna.dlna.upnp.transport

import org.jupnp.transport.spi.StreamServerConfiguration

class MyStreamServerConfiguration(
    private val listenPort: Int
) : StreamServerConfiguration {
    override fun getListenPort() = listenPort
}

