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
    val address: InetAddress = guessInetAddress()

    @PostConstruct
    fun init() {
        logger.info("Using server address: {} and port {}", address.hostAddress, port)
    }

    private fun guessInetAddress(): InetAddress {
        try {
            return if (networkInterface.isNotEmpty()) {
                logger.debug { "Using network interface $networkInterface" }
                val iface = NetworkInterface.getByName(networkInterface)
                    ?: throw RuntimeException("Could not find network interface $networkInterface")

                iface.inetAddresses.toList().filterIsInstance<Inet4Address>().first()
            } else {
                logger.info { "No network interface given, using default local address" }
                InetAddress.getLocalHost()
            }.also {
                logger.debug { "Found local address ${it.hostAddress}" }
            }
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        } catch (e: SocketException) {
            throw RuntimeException(e)
        }
    }

    companion object : KLogging()
}
