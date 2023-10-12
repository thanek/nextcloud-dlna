package net.schowek.nextclouddlna.upnp

import mu.KLogging
import net.schowek.nextclouddlna.DlnaService
import org.jupnp.model.message.StreamRequestMessage
import org.jupnp.model.message.StreamResponseMessage
import org.jupnp.model.message.UpnpResponse
import org.jupnp.protocol.ProtocolFactory
import org.jupnp.transport.spi.UpnpStream
import org.springframework.stereotype.Component

@Component
class UpnpStreamProcessor(
    private val dlna: DlnaService
) {
    private var upnpStream: UpnpStreamImpl? = null

    fun processMessage(requestMsg: StreamRequestMessage): StreamResponseMessage {
        logger.debug { "Processing $requestMsg" }
        var response = getUpnpStream().process(requestMsg)
        if (response == null) {
            response = StreamResponseMessage(UpnpResponse.Status.NOT_FOUND)
        }
        return response
    }

    private fun getUpnpStream(): UpnpStreamImpl {
        if (upnpStream == null) {
            upnpStream = UpnpStreamImpl(dlna.upnpService.protocolFactory)
        }
        return upnpStream!!
    }

    inner class UpnpStreamImpl(protocolFactory: ProtocolFactory) : UpnpStream(protocolFactory) {
        override fun run() {
        }
    }

    companion object : KLogging()
}