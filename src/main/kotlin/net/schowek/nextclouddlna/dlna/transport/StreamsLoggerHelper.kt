package net.schowek.nextclouddlna.dlna.transport

import io.micrometer.common.util.StringUtils
import mu.KLogging
import org.jupnp.model.message.StreamRequestMessage
import org.jupnp.model.message.StreamResponseMessage
import org.jupnp.model.message.UpnpMessage


class StreamsLoggerHelper {
    companion object : KLogging() {
        private const val HTTPSERVER_REQUEST_BEGIN =
            "================================== HTTPSERVER REQUEST BEGIN ====================================="
        private const val HTTPSERVER_REQUEST_END =
            "================================== HTTPSERVER REQUEST END ======================================="
        private const val HTTPSERVER_RESPONSE_BEGIN =
            "================================== HTTPSERVER RESPONSE BEGIN ===================================="
        private const val HTTPSERVER_RESPONSE_END =
            "================================== HTTPSERVER RESPONSE END ======================================"
        private const val HTTPCLIENT_REQUEST_BEGIN =
            "==================================== HTTPCLIENT REQUEST BEGIN ===================================="
        private const val HTTPCLIENT_REQUEST_END =
            "==================================== HTTPCLIENT REQUEST END ======================================"
        private const val HTTPCLIENT_RESPONSE_BEGIN =
            "==================================== HTTPCLIENT RESPONSE BEGIN ==================================="
        private const val HTTPCLIENT_RESPONSE_END =
            "==================================== HTTPCLIENT RESPONSE END ====================================="

        fun logStreamServerRequestMessage(requestMessage: StreamRequestMessage) {
            val formattedRequest = getFormattedRequest(requestMessage)
            val formattedHeaders = getFormattedHeaders(requestMessage)
            val formattedBody = getFormattedBody(requestMessage)
            logger.trace(
                "Received a request from {}:\n{}\n{}{}{}{}",
                requestMessage.connection.remoteAddress.hostAddress,
                HTTPSERVER_REQUEST_BEGIN,
                formattedRequest,
                formattedHeaders,
                formattedBody,
                HTTPSERVER_REQUEST_END
            )
        }

        fun logStreamServerResponseMessage(
            responseMessage: StreamResponseMessage,
            requestMessage: StreamRequestMessage
        ) {
            val formattedResponse = getFormattedResponse(responseMessage)
            val formattedHeaders = getFormattedHeaders(responseMessage)
            val formattedBody = getFormattedBody(responseMessage)
            logger.trace(
                "Send a response to {}:\n{}\n{}{}{}{}",
                requestMessage.connection.remoteAddress.hostAddress,
                HTTPSERVER_RESPONSE_BEGIN,
                formattedResponse,
                formattedHeaders,
                formattedBody,
                HTTPSERVER_RESPONSE_END
            )
        }

        fun logStreamClientRequestMessage(requestMessage: StreamRequestMessage) {
            val formattedRequest = getFormattedRequest(requestMessage)
            val formattedHeaders = getFormattedHeaders(requestMessage)
            val formattedBody = getFormattedBody(requestMessage)
            logger.trace(
                "Send a request to {}:\n{}\n{}{}{}{}",
                requestMessage.uri.host,
                HTTPCLIENT_REQUEST_BEGIN,
                formattedRequest,
                formattedHeaders,
                formattedBody,
                HTTPCLIENT_REQUEST_END
            )
        }

        fun logStreamClientResponseMessage(
            responseMessage: StreamResponseMessage,
            requestMessage: StreamRequestMessage?
        ) {
            val formattedResponse = getFormattedResponse(responseMessage)
            val formattedHeaders = getFormattedHeaders(responseMessage)
            val formattedBody = getFormattedBody(responseMessage)
            logger.trace(
                "Received a response from {}:\n{}\n{}{}{}{}",
                requestMessage?.uri?.host,
                HTTPCLIENT_RESPONSE_BEGIN,
                formattedResponse,
                formattedHeaders,
                formattedBody,
                HTTPCLIENT_RESPONSE_END
            )
        }

        private fun getFormattedRequest(requestMessage: StreamRequestMessage): String {
            val request = StringBuilder()
            request.append(requestMessage.operation.httpMethodName).append(" ").append(requestMessage.uri.path)
            request.append(" HTTP/1.").append(requestMessage.operation.httpMinorVersion).append("\n")
            return request.toString()
        }

        private fun getFormattedResponse(responseMessage: StreamResponseMessage): String {
            val response = StringBuilder()
            response.append("HTTP/1.").append(responseMessage.operation.httpMinorVersion)
            response.append(" ").append(responseMessage.operation.responseDetails).append("\n")
            return response.toString()
        }

        private fun getFormattedHeaders(message: UpnpMessage<*>): String {
            val headers = StringBuilder()
            for ((key, value1) in message.headers) {
                if (StringUtils.isNotEmpty(key)) {
                    for (value in value1) {
                        headers.append("  ").append(key).append(": ").append(value).append("\n")
                    }
                }
            }
            if (headers.isNotEmpty()) {
                headers.insert(0, "\nHEADER:\n")
            }
            return headers.toString()
        }

        private fun getFormattedBody(message: UpnpMessage<*>): String {
            var formattedBody = ""
            //message.isBodyNonEmptyString throw StringIndexOutOfBoundsException if string is empty
            try {
                val bodyNonEmpty = message.body != null &&
                        (message.body is String && (message.body as String).isNotEmpty()
                                || message.body is ByteArray && (message.body as ByteArray).isNotEmpty())
                if (bodyNonEmpty && message.isBodyNonEmptyString) {
                    formattedBody = message.bodyString
                }
            } catch (e: Exception) {
                formattedBody = ""
            }
            formattedBody = if (StringUtils.isNotEmpty(formattedBody)) {
                "\nCONTENT:\n$formattedBody"
            } else {
                ""
            }
            return formattedBody
        }
    }
}

