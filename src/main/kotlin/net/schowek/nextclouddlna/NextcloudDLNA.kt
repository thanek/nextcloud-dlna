package net.schowek.nextclouddlna

import jakarta.annotation.PreDestroy
import net.schowek.nextclouddlna.dlna.DlnaService
import org.jupnp.UpnpService
import org.springframework.stereotype.Component


@Component
class NextcloudDLNA(
    private val dlnaService: DlnaService
) {
    private val upnpService: UpnpService = dlnaService.start()

    @PreDestroy
    fun destroy() {
        upnpService.shutdown()
    }
}

