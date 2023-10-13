package net.schowek.nextclouddlna.dlna

import jakarta.servlet.http.HttpServletRequest
import mu.KLogging
import org.jupnp.model.message.*
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.URI

@Component
class StreamRequestMapper {
    fun map(request: HttpServletRequest): StreamRequestMessage {
        val requestMessage = StreamRequestMessage(
            UpnpRequest.Method.getByHttpName(request.method),
            URI(request.requestURI),
            // TODO check if request is binary and create body as unwrapped byteArray
            String(request.inputStream.readBytes())
        )
        if (requestMessage.operation.method == UpnpRequest.Method.UNKNOWN) {
            logger.warn("Method not supported by UPnP stack: {}", request.method)
            throw RuntimeException("Method not supported: {}" + request.method)
        }

        requestMessage.connection = MyHttpServerConnection(request)
        requestMessage.headers = createHeaders(request)
        return requestMessage
    }

    private fun createHeaders(request: HttpServletRequest): UpnpHeaders {
        val headers = mutableMapOf<String, List<String>>()
        request.headerNames?.let {
            while (it.hasMoreElements()) {
                with(it.nextElement()) {
                    headers[this] = listOf(request.getHeader(this))
                }
            }
        }
        return UpnpHeaders(headers)
    }

    inner class MyHttpServerConnection(
        private val request: HttpServletRequest
    ) : Connection {
        override fun isOpen() = true

        override fun getRemoteAddress(): InetAddress? =
            request.remoteAddr?.let { InetAddress.getByName(request.remoteAddr) }

        override fun getLocalAddress(): InetAddress? =
            request.localAddr?.let { InetAddress.getByName(request.localAddr) }
    }

    companion object : KLogging()
}

