package net.schowek.nextclouddlna.dlna.transport

import org.jupnp.transport.spi.AbstractStreamClientConfiguration
import java.util.concurrent.ExecutorService


class ApacheStreamClientConfiguration : AbstractStreamClientConfiguration {
    var maxTotalConnections = 1024

    var maxTotalPerRoute = 100

    var contentCharset = "UTF-8" // UDA spec says it's always UTF-8 entity content

    constructor(timeoutExecutorService: ExecutorService?) : super(timeoutExecutorService)
    constructor(timeoutExecutorService: ExecutorService?, timeoutSeconds: Int) : super(
        timeoutExecutorService,
        timeoutSeconds
    )

    val socketBufferSize: Int get() = -1

    val requestRetryCount: Int get() = 0
}

