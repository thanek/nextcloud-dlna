package net.schowek.nextclouddlna.dlna.transport

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.jupnp.model.message.Connection
import org.jupnp.transport.Router
import org.jupnp.transport.spi.InitializationException
import org.jupnp.transport.spi.StreamServer
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress


class MyStreamServerImpl(private val configuration: MyStreamServerConfiguration) :
    StreamServer<MyStreamServerConfiguration> {
    protected var server: HttpServer? = null

    @Synchronized
    @Throws(InitializationException::class)
    override fun init(bindAddress: InetAddress, router: Router) {
        try {
            val socketAddress = InetSocketAddress(bindAddress, configuration.getListenPort())
            server = HttpServer.create(socketAddress, configuration.tcpConnectionBacklog)
            server!!.createContext("/", MyRequestHttpHandler(router))
            logger.info("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *")
            logger.info("Created server (for receiving TCP streams) on: " + server!!.address)
            logger.info("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *")
        } catch (ex: Exception) {
            throw InitializationException("Could not initialize " + javaClass.simpleName + ": " + ex.toString(), ex)
        }
    }

    @Synchronized
    override fun getPort(): Int {
        return server!!.address.port
    }

    override fun getConfiguration(): MyStreamServerConfiguration {
        return configuration
    }

    @Synchronized
    override fun run() {
        logger.info("Starting StreamServer...")
        // Starts a new thread but inherits the properties of the calling thread
        server!!.start()
    }

    @Synchronized
    override fun stop() {
        logger.info("Stopping StreamServer...")
        if (server != null) {
            server!!.stop(1)
        }
    }

    inner class MyRequestHttpHandler(private val router: Router) : HttpHandler {
        // This is executed in the request receiving thread!
        @Throws(IOException::class)
        override fun handle(httpExchange: HttpExchange) {
            // And we pass control to the service, which will (hopefully) start a new thread immediately so we can
            // continue the receiving thread ASAP
            logger.info("Received HTTP exchange: " + httpExchange.requestMethod + " " + httpExchange.requestURI + " from " + httpExchange.remoteAddress)
            router.received(
                object : MyHttpExchangeUpnpStream(router.protocolFactory, httpExchange) {
                    override fun createConnection(): Connection {
                        return MyHttpServerConnection(httpExchange)
                    }
                }
            )
        }
    }

    /**
     * Logs a warning and returns `true`, we can't access the socket using the awful JDK webserver API.
     * Override this method if you know how to do it.
     */
    protected fun isConnectionOpen(exchange: HttpExchange?): Boolean {
        logger.warn("Can't check client connection, socket access impossible on JDK webserver!")
        return true
    }

    protected inner class MyHttpServerConnection(protected var exchange: HttpExchange) : Connection {
        override fun isOpen(): Boolean {
            return isConnectionOpen(exchange)
        }

        override fun getRemoteAddress(): InetAddress? {
            return if (exchange.remoteAddress != null) exchange.remoteAddress.address else null
        }

        override fun getLocalAddress(): InetAddress? {
            return if (exchange.localAddress != null) exchange.localAddress.address else null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MyStreamServerImpl::class.java)
    }
}

