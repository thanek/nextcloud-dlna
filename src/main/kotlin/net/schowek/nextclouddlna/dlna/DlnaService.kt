package net.schowek.nextclouddlna.dlna

import jakarta.annotation.PreDestroy
import mu.KLogging
import org.jupnp.UpnpService
import org.jupnp.model.message.StreamRequestMessage
import org.jupnp.model.message.StreamResponseMessage
import org.jupnp.model.message.UpnpResponse
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component
class DlnaService(
    private val upnpService: UpnpService,
    private val mediaServer: MediaServer
) {
    fun start() {
        upnpService.startup()
        upnpService.registry.addDevice(mediaServer.device)
    }

    @EventListener(condition = "!@environment.acceptsProfiles('integration')")
    fun handleContextRefresh(event: ContextRefreshedEvent) {
        start()
    }

    @PreDestroy
    fun destroy() {
        upnpService.shutdown()
    }

    fun processRequest(requestMsg: StreamRequestMessage): StreamResponseMessage {
        logger.debug { "Processing $requestMsg" }
        return with(upnpService.protocolFactory.createReceivingSync(requestMsg)) {
            run()
            outputMessage
                ?: StreamResponseMessage(UpnpResponse.Status.NOT_FOUND).also {
                    logger.warn { "Could not get response for ${requestMsg.operation.method} ${requestMsg}" }
                }
        }.also {
            logger.debug { "Response: ${it.operation.statusCode} ${it.body}" }
        }
    }

    companion object : KLogging()
}
