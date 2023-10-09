package net.schowek.nextclouddlna.dlna.transport

import net.schowek.nextclouddlna.util.Logging
import org.apache.http.*
import org.apache.http.client.ResponseHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.config.ConnectionConfig
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import org.jupnp.http.Headers
import org.jupnp.model.message.*
import org.jupnp.model.message.header.UpnpHeader
import org.jupnp.transport.spi.AbstractStreamClient
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.Callable


class ApacheStreamClient(
    private val configuration: ApacheStreamClientConfiguration
) : Logging, AbstractStreamClient<ApacheStreamClientConfiguration?, HttpRequestBase>() {
    private val clientConnectionManager: PoolingHttpClientConnectionManager
    private val httpClient: CloseableHttpClient

    init {
        val connectionConfigBuilder = ConnectionConfig.custom().also {
            it.setCharset(Charset.forName(configuration.contentCharset))
            if (configuration.socketBufferSize != -1) {
                it.setBufferSize(configuration.socketBufferSize)
            }
        }
        val requestConfigBuilder = RequestConfig.custom().also {
            it.setExpectContinueEnabled(false)
            // These are some safety settings, we should never run into these timeouts as we
            // do our own expiration checking
            it.setConnectTimeout((configuration.timeoutSeconds + 5) * 1000)
            it.setSocketTimeout((configuration.timeoutSeconds + 5) * 1000)
        }

        // Only register 80, not 443 and SSL
        val registry = RegistryBuilder.create<ConnectionSocketFactory>()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .build()
        clientConnectionManager = PoolingHttpClientConnectionManager(registry).also {
            it.maxTotal = configuration.maxTotalConnections
            it.defaultMaxPerRoute = configuration.maxTotalPerRoute
        }
        val defaultHttpRequestRetryHandler = if (configuration.requestRetryCount != -1) {
            DefaultHttpRequestRetryHandler(configuration.requestRetryCount, false)
        } else {
            DefaultHttpRequestRetryHandler()
        }
        httpClient = HttpClients
            .custom()
            .setDefaultConnectionConfig(connectionConfigBuilder.build())
            .setConnectionManager(clientConnectionManager)
            .setDefaultRequestConfig(requestConfigBuilder.build())
            .setRetryHandler(defaultHttpRequestRetryHandler)
            .build()
    }

    override fun getConfiguration(): ApacheStreamClientConfiguration {
        return configuration
    }

    override fun createRequest(requestMessage: StreamRequestMessage): HttpRequestBase {
        val requestOperation = requestMessage.operation
        val request: HttpRequestBase
        when (requestOperation.method) {
            UpnpRequest.Method.GET -> {
                request = HttpGet(requestOperation.uri)
            }

            UpnpRequest.Method.SUBSCRIBE -> {
                request = object : HttpGet(requestOperation.uri) {
                    override fun getMethod(): String {
                        return UpnpRequest.Method.SUBSCRIBE.httpName
                    }
                }
            }

            UpnpRequest.Method.UNSUBSCRIBE -> {
                request = object : HttpGet(requestOperation.uri) {
                    override fun getMethod(): String {
                        return UpnpRequest.Method.UNSUBSCRIBE.httpName
                    }
                }
            }

            UpnpRequest.Method.POST -> {
                request = HttpPost(requestOperation.uri)
                (request as HttpEntityEnclosingRequestBase).entity = createHttpRequestEntity(requestMessage)
            }

            UpnpRequest.Method.NOTIFY -> {
                request = object : HttpPost(requestOperation.uri) {
                    override fun getMethod(): String {
                        return UpnpRequest.Method.NOTIFY.httpName
                    }
                }
                (request as HttpEntityEnclosingRequestBase).entity = createHttpRequestEntity(requestMessage)
            }

            else -> throw RuntimeException("Unknown HTTP method: " + requestOperation.httpMethodName)
        }

        // Headers
        // Add the default user agent if not already set on the message
        if (!requestMessage.headers.containsKey(UpnpHeader.Type.USER_AGENT)) {
            request.setHeader(
                "User-Agent", getConfiguration().getUserAgentValue(
                    requestMessage.udaMajorVersion,
                    requestMessage.udaMinorVersion
                )
            )
        }
        if (requestMessage.operation.httpMinorVersion == 0) {
            request.protocolVersion = HttpVersion.HTTP_1_0
        } else {
            request.protocolVersion = HttpVersion.HTTP_1_1
            // This closes the http connection immediately after the call.
            request.addHeader("Connection", "close")
        }
        addHeaders(request, requestMessage.headers)
        return request
    }

    override fun createCallable(
        requestMessage: StreamRequestMessage,
        request: HttpRequestBase
    ): Callable<StreamResponseMessage> {
        return Callable<StreamResponseMessage> {
            logger.trace("Sending HTTP request: $requestMessage")
            if (logger.isTraceEnabled) {
                StreamsLoggerHelper.logStreamClientRequestMessage(requestMessage)
            }
            httpClient.execute<StreamResponseMessage>(request, createResponseHandler(requestMessage))
        }
    }

    override fun abort(request: HttpRequestBase) {
        request.abort()
    }

    override fun logExecutionException(t: Throwable): Boolean {
        if (t is IllegalStateException) {
            // TODO: Document when/why this happens and why we can ignore it, violating the
            // logging rules of the StreamClient#sendRequest() method
            logger.trace("Illegal state: {}", t.message)
            return true
        } else if (t is NoHttpResponseException) {
            logger.trace("No Http Response: {}", t.message)
            return true
        }
        return false
    }

    override fun stop() {
        logger.trace("Shutting down HTTP client connection manager/pool")
        clientConnectionManager.shutdown()
    }

    private fun createHttpRequestEntity(upnpMessage: UpnpMessage<*>): HttpEntity {
        return if (upnpMessage.bodyType == UpnpMessage.BodyType.BYTES) {
            logger.trace("Preparing HTTP request entity as byte[]")
            ByteArrayEntity(upnpMessage.bodyBytes)
        } else {
            logger.trace("Preparing HTTP request entity as string")
            var charset = upnpMessage.contentTypeCharset
            if (charset == null) {
                charset = "UTF-8"
            }
            try {
                StringEntity(upnpMessage.bodyString, charset)
            } catch (ex: UnsupportedCharsetException) {
                logger.trace("HTTP request does not support charset: {}", charset)
                throw RuntimeException(ex)
            }
        }
    }

    private fun createResponseHandler(requestMessage: StreamRequestMessage?): ResponseHandler<StreamResponseMessage> {
        return ResponseHandler<StreamResponseMessage> { httpResponse: HttpResponse ->
            val statusLine = httpResponse.statusLine
            logger.trace("Received HTTP response: $statusLine")

            // Status
            val responseOperation = UpnpResponse(statusLine.statusCode, statusLine.reasonPhrase)

            // Message
            val responseMessage = StreamResponseMessage(responseOperation)

            // Headers
            responseMessage.headers = UpnpHeaders(getHeaders(httpResponse))

            // Body
            val entity = httpResponse.entity
            if (entity == null || entity.contentLength == 0L) {
                logger.trace("HTTP response message has no entity")
                return@ResponseHandler responseMessage
            }
            val data = EntityUtils.toByteArray(entity)
            if (data != null) {
                if (responseMessage.isContentTypeMissingOrText) {
                    logger.trace("HTTP response message contains text entity")
                    responseMessage.setBodyCharacters(data)
                } else {
                    logger.trace("HTTP response message contains binary entity")
                    responseMessage.setBody(UpnpMessage.BodyType.BYTES, data)
                }
            } else {
                logger.trace("HTTP response message has no entity")
            }
            if (logger.isTraceEnabled) {
                StreamsLoggerHelper.logStreamClientResponseMessage(responseMessage, requestMessage)
            }
            responseMessage
        }
    }

    companion object {
        private fun addHeaders(httpMessage: HttpMessage, headers: Headers) {
            for ((key, value1) in headers) {
                for (value in value1) {
                    httpMessage.addHeader(key, value)
                }
            }
        }

        private fun getHeaders(httpMessage: HttpMessage): Headers {
            val headers = Headers()
            for (header in httpMessage.allHeaders) {
                headers.add(header.name, header.value)
            }
            return headers
        }
    }
}

