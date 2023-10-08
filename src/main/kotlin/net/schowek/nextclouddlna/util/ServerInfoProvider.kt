package net.schowek.nextclouddlna.util

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.*
import java.util.*

@Component
class ServerInfoProvider(
    @param:Value("\${server.port}") val port: Int,
    @param:Value("\${server.interface}") private val networkInterface: String
) {
    var logger = LoggerFactory.getLogger(ServerInfoProvider::class.java)
    var address: InetAddress? = null

    @PostConstruct
    fun init() {
        address = guessInetAddress()
        logger.info("Using server address: {} and port {}", address!!.hostAddress, port)
    }

    private fun guessInetAddress(): InetAddress {
        return try {
            val en0 = NetworkInterface.getByName(networkInterface).inetAddresses
            while (en0.hasMoreElements()) {
                val x = en0.nextElement()
                if (x is Inet4Address) {
                    return x
                }
            }
            InetAddress.getLocalHost()
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        } catch (e: SocketException) {
            throw RuntimeException(e)
        }
    }
}
