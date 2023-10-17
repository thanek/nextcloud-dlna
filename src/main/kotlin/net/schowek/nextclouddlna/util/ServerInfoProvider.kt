package net.schowek.nextclouddlna.util

import jakarta.annotation.PostConstruct
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.*
import java.util.*

interface ServerInfoProvider {
    val host: String
    val port: Int
}

@Component
@Profile("!integration")
class ServerInfoProviderImpl(
    @param:Value("\${server.port}") override val port: Int,
    @param:Value("\${server.interface}") private val networkInterface: String
) : ServerInfoProvider {
    override val host: String get() = address!!.hostAddress
    var address: InetAddress? = null

    @PostConstruct
    fun init() {
        address = guessInetAddress()
        logger.info("Using server address: {} and port {}", address!!.hostAddress, port)
    }

    private fun guessInetAddress(): InetAddress {
        return try {
            val iface = NetworkInterface.getByName(networkInterface)
                ?: throw RuntimeException("Could not find network interface $networkInterface")
            val addresses = iface.inetAddresses
            while (addresses.hasMoreElements()) {
                val x = addresses.nextElement()
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

    companion object : KLogging()
}
