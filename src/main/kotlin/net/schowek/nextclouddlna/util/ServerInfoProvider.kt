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
    override val host: String get() = address.hostAddress
    private val address: InetAddress = getInetAddress()

    init {
        logger.info("Using server address: ${address.hostAddress} and port $port")
    }

    private fun getInetAddress(): InetAddress {
        try {
            return if (networkInterface.isNotEmpty()) {
                logger.debug { "Using network interface $networkInterface" }
                val iface = NetworkInterface.getByName(networkInterface)
                    ?: throw RuntimeException("Could not find network interface $networkInterface")

                iface.inetAddresses.toList().filterIsInstance<Inet4Address>().first()
            } else {
                logger.info { "No network interface name given, trying to use default local address" }
                guessLocalAddress()
            }.also {
                logger.debug { "Found local address ${it.hostAddress}" }
            }
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        } catch (e: SocketException) {
            throw RuntimeException(e)
        }
    }

    private fun guessLocalAddress() = try {
        DatagramSocket().use { s ->
            s.connect(InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1)), 80)
            s.localAddress
        }
    } catch (e: Exception) {
        logger.warn { e.message }
        InetAddress.getLocalHost()
    }

    companion object : KLogging()
}
