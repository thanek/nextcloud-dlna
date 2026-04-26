package net.schowek.nextclouddlna.dlna.upnp.transport

import mu.KLogging
import org.jupnp.transport.Router
import org.jupnp.transport.spi.StreamServer
import java.net.InetAddress


/**
 * Intentional no-op implementation of JUPnP's StreamServer.
 *
 * JUPnP normally starts its own HTTP server here, but in this application all
 * incoming HTTP traffic is handled by Spring/Undertow. Requests are routed from
 * UpnpController → StreamMessageMapper → DlnaService, which feeds them directly
 * into JUPnP's protocol stack. This stub satisfies the interface contract while
 * preventing JUPnP from binding its own server socket on the same port.
 */
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

