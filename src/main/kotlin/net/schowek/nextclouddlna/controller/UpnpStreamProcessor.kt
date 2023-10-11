import mu.KLogging
import net.schowek.nextclouddlna.NextcloudDLNA
import org.jupnp.model.message.StreamRequestMessage
import org.jupnp.model.message.StreamResponseMessage
import org.jupnp.model.message.UpnpResponse
import org.jupnp.transport.spi.UpnpStream


class UpnpStreamProcessor(
    dlna: NextcloudDLNA
) : UpnpStream(dlna.upnpService.protocolFactory) {

    fun processMessage(requestMsg: StreamRequestMessage): StreamResponseMessage {
        logger.debug { "Processing $requestMsg" }
        var response = super.process(requestMsg)
        if (response == null) {
            response = StreamResponseMessage(UpnpResponse.Status.NOT_FOUND)
        }
        return response
    }

    override fun run() {
    }

    companion object : KLogging()
}