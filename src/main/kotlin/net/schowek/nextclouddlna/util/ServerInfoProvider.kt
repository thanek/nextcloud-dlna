package net.schowek.nextclouddlna.util

import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.*


interface AddressResolver {
    fun resolve(interfaceName: String): InetAddress
}

interface LocalAddressGuesser {
    fun guess(): InetAddress
}

interface ServerInfoProvider {
    val host: String
    val port: Int
}


@Component
@Profile("!integration")
class ServerInfoProviderImpl(
    @param:Value("\${server.port}") override val port: Int,
    @param:Value("\${server.interface}") private val networkInterface: String,
    private val addressResolver: AddressResolver,
    private val localAddressGuesser: LocalAddressGuesser
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
                val iface = addressResolver.resolve(networkInterface)
                logger.debug { "Found local address ${iface.hostAddress}" }
                iface
            } else {
                logger.info { "No network interface name given, trying to use default local address" }
                localAddressGuesser.guess()
            }
        } catch (e: UnknownHostException) {
            throw RuntimeException(e)
        } catch (e: SocketException) {
            throw RuntimeException(e)
        }
    }

    companion object : KLogging()
}


@Component
class NetworkInterfaceAddressResolver : AddressResolver {
    override fun resolve(interfaceName: String): InetAddress {
        val iface = NetworkInterface.getByName(interfaceName)
            ?: throw RuntimeException("Could not find network interface $interfaceName")
        return iface.inetAddresses.toList().filterIsInstance<Inet4Address>().first()
    }
}


@Component
class DefaultLocalAddressGuesser : LocalAddressGuesser {
    override fun guess(): InetAddress = try {
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
