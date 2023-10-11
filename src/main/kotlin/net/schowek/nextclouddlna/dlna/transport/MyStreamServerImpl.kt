package net.schowek.nextclouddlna.dlna.transport

import com.sun.net.httpserver.HttpExchange
import mu.KLogging
import org.jupnp.transport.Router
import org.jupnp.transport.spi.StreamServer
import java.net.InetAddress


class MyStreamServerImpl(
    private val configuration: MyStreamServerConfiguration
) : StreamServer<MyStreamServerConfiguration> {
    override fun init(bindAddress: InetAddress, router: Router) {
    }

    override fun getPort(): Int {
        return configuration.listenPort;
    }

    override fun getConfiguration(): MyStreamServerConfiguration {
        return configuration
    }

    override fun run() {
    }

    override fun stop() {
    }

    private fun isConnectionOpen(exchange: HttpExchange?): Boolean {
        logger.warn("Can't check client connection, socket access impossible on JDK webserver!")
        return true
    }

    companion object: KLogging()
}

