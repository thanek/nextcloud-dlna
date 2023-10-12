package net.schowek.nextclouddlna.upnp

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
        with(request.headerNames) {
            if (this != null) {
                while (hasMoreElements()) {
                    with(nextElement()) {
                        headers[this] = listOf(request.getHeader(this))
                    }
                }
            }
        }
        return UpnpHeaders(headers)
    }

    inner class MyHttpServerConnection(
        private val request: HttpServletRequest
    ) : Connection {
        override fun isOpen(): Boolean {
            return true
        }

        override fun getRemoteAddress(): InetAddress? {
            return if (request.remoteAddr != null) InetAddress.getByName(request.remoteAddr) else null
        }

        override fun getLocalAddress(): InetAddress? {
            return if (request.localAddr != null) InetAddress.getByName(request.localAddr) else null
        }
    }

    companion object : KLogging()
}

