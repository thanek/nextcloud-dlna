package net.schowek.nextclouddlna.dlna.upnp.transport

import org.jupnp.transport.spi.AbstractStreamClientConfiguration
import java.util.concurrent.ExecutorService


class ApacheStreamClientConfiguration(
    timeoutExecutorService: ExecutorService
) : AbstractStreamClientConfiguration(timeoutExecutorService) {
    val maxTotalConnections = 1024
    val maxTotalPerRoute = 100
    val contentCharset = "UTF-8" // UDA spec says it's always UTF-8 entity content

    val socketBufferSize: Int get() = -1

    val requestRetryCount: Int get() = 0
}

