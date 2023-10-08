package net.schowek.nextclouddlna.dlna.transport

import com.sun.net.httpserver.HttpExchange
import org.jupnp.model.message.*
import org.jupnp.protocol.ProtocolFactory
import org.jupnp.transport.spi.UpnpStream
import org.jupnp.util.io.IO
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection


abstract class MyHttpExchangeUpnpStream(
    protocolFactory: ProtocolFactory?,
    val httpExchange: HttpExchange
) : UpnpStream(protocolFactory) {

    override fun run() {
        try {
            val xchng = httpExchange

            // Status
            val requestMessage = StreamRequestMessage(
                UpnpRequest.Method.getByHttpName(xchng.requestMethod),
                xchng.requestURI
            )
            if (requestMessage.operation.method == UpnpRequest.Method.UNKNOWN) {
                logger.warn("Method not supported by UPnP stack: {}", xchng.requestMethod)
                throw RuntimeException("Method not supported: {}" + xchng.requestMethod)
            }

            // Protocol
            requestMessage.operation.httpMinorVersion = if (xchng.protocol.uppercase() == "HTTP/1.1") 1 else 0

            // Connection wrapper
            requestMessage.connection = createConnection()

            // Headers
            requestMessage.headers = UpnpHeaders(xchng.requestHeaders)

            // Body
            val bodyBytes: ByteArray
            var inputStream: InputStream? = null
            try {
                inputStream = xchng.requestBody
                bodyBytes = IO.readBytes(inputStream)
            } finally {
                inputStream?.close()
            }
            logger.info(" Reading request body bytes: " + bodyBytes.size)
            if (bodyBytes.isNotEmpty() && requestMessage.isContentTypeMissingOrText) {
                logger.debug("Request contains textual entity body, converting then setting string on message")
                requestMessage.setBodyCharacters(bodyBytes)
            } else if (bodyBytes.isNotEmpty()) {
                logger.debug("Request contains binary entity body, setting bytes on message")
                requestMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes)
            } else {
                logger.debug("Request did not contain entity body")
            }
            if (bodyBytes.isNotEmpty()) {
                logger.info(" Request body: " + requestMessage.body)
            }
            val responseMessage = process(requestMessage)

            // Return the response
            if (responseMessage != null) {
                // Headers
                xchng.responseHeaders.putAll(responseMessage.headers)

                // Body
                val responseBodyBytes = if (responseMessage.hasBody()) responseMessage.bodyBytes else null
                val contentLength = responseBodyBytes?.size ?: -1
                logger.info("Sending HTTP response message: $responseMessage with content length: $contentLength")
                xchng.sendResponseHeaders(responseMessage.operation.statusCode, contentLength.toLong())
                if (responseBodyBytes!!.isNotEmpty()) {
                    logger.debug(" Response body: " + responseMessage.body)
                }
                if (contentLength > 0) {
                    logger.debug("Response message has body, writing bytes to stream...")
                    var outputStream: OutputStream? = null
                    try {
                        outputStream = xchng.responseBody
                        IO.writeBytes(outputStream, responseBodyBytes)
                        outputStream.flush()
                    } finally {
                        outputStream?.close()
                    }
                }
            } else {
                // If it's null, it's 404, everything else needs a proper httpResponse
                logger.info("Sending HTTP response status: " + HttpURLConnection.HTTP_NOT_FOUND)
                xchng.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1)
            }
            responseSent(responseMessage)
        } catch (t: Throwable) {

            // You definitely want to catch all Exceptions here, otherwise the server will
            // simply close the socket and you get an "unexpected end of file" on the client.
            // The same is true if you just rethrow an IOException - it is a mystery why it
            // is declared then on the HttpHandler interface if it isn't handled in any
            // way... so we always do error handling here.

            // TODO: We should only send an error if the problem was on our side
            // You don't have to catch Throwable unless, like we do here in unit tests,
            // you might run into Errors as well (assertions).
            logger.warn("Exception occurred during UPnP stream processing:", t)
            try {
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1)
            } catch (ex: IOException) {
                logger.warn("Couldn't send error response: ", ex)
            }
            responseException(t)
        }
    }

    protected abstract fun createConnection(): Connection?

    companion object {
        private val logger = LoggerFactory.getLogger(MyHttpExchangeUpnpStream::class.java)
    }
}

