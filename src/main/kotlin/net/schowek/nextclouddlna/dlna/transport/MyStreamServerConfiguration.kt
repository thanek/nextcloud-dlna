package net.schowek.nextclouddlna.dlna.transport

import org.jupnp.transport.spi.StreamServerConfiguration

class MyStreamServerConfiguration(
    private val listenPort: Int
) : StreamServerConfiguration {
    override fun getListenPort() = listenPort
}

