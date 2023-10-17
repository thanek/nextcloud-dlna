package net.schowek.nextclouddlna.dlna

import jakarta.servlet.http.HttpServletRequest
import mu.KLogging
import org.jupnp.model.message.StreamRequestMessage
import org.jupnp.model.message.StreamResponseMessage
import org.jupnp.model.message.UpnpHeaders
import org.jupnp.model.message.UpnpRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.net.URI

@Component
class StreamMessageMapper {
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

        requestMessage.headers = upnpHeaders(request)
        return requestMessage
    }

    fun map(response: StreamResponseMessage): ResponseEntity<Any> {
        return with(response) {
            ResponseEntity(
                body,
                HttpHeaders().also { h ->
                    headers.entries.forEach { e ->
                        h.add(e.key, e.value.joinToString { it })
                    }
                },
                HttpStatusCode.valueOf(operation.statusCode)
            )
        }
    }

    private fun upnpHeaders(request: HttpServletRequest): UpnpHeaders {
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

    companion object : KLogging()
}

