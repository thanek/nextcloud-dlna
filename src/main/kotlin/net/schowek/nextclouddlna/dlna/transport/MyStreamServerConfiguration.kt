package net.schowek.nextclouddlna.dlna.transport

import org.jupnp.transport.spi.StreamServerConfiguration

class MyStreamServerConfiguration(
    private val listenPort: Int
) : StreamServerConfiguration {
    val tcpConnectionBacklog = 0
    override fun getListenPort(): Int {
        return listenPort
    }
}

