package net.schowek.nextclouddlna.controller

import UpnpStreamProcessor
import jakarta.servlet.http.HttpServletRequest
import mu.KLogging
import net.schowek.nextclouddlna.NextcloudDLNA
import net.schowek.nextclouddlna.dlna.media.MediaServer
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.*
import org.springframework.web.bind.annotation.ResponseBody


@Controller
class DLNAController(
    private val dlna: NextcloudDLNA,
    private val streamRequestMapper: StreamRequestMapper
) {
    @RequestMapping(
        method = [GET, HEAD], value = ["/dev/{uid}/icon.png"],
        produces = [APPLICATION_OCTET_STREAM_VALUE]
    )
    @ResponseBody
    fun handleGetIcon(
        @PathVariable("uid") uid: String,
        request: HttpServletRequest
    ): Resource {
        logger.info { "GET ICON request from ${request.remoteAddr}: ${request.requestURI}" }
        return InputStreamResource(MediaServer.iconResource());
    }

    @RequestMapping(
        method = [GET, HEAD], value = [
            "/dev/{uid}/desc",
            "/dev/{uid}/svc/upnp-org/ContentDirectory/desc",
            "/dev/{uid}/svc/upnp-org/ConnectionManager/desc"
        ],
        produces = ["application/xml;charset=utf8", "text/xml;charset=utf8"]
    )
    fun handleGet(
        @PathVariable("uid") uid: String,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        logger.info { "GET request from ${request.remoteAddr}: ${request.requestURI}" }
        val r = UpnpStreamProcessor(dlna).processMessage(streamRequestMapper.map(request))
        return ResponseEntity(
            r.body,
            HttpHeaders().also { h -> r.headers.entries.forEach { h.add(it.key, it.value.joinToString { it }) } },
            HttpStatusCode.valueOf(r.operation.statusCode)
        )
    }

    @RequestMapping(
        method = [POST], value = [
            "/dev/{uid}/svc/upnp-org/ContentDirectory/action"
        ],
        produces = ["application/xml;charset=utf8", "text/xml;charset=utf8"]
    )
    fun handlePost(
        @PathVariable("uid") uid: String,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        logger.info { "POST request from ${request.remoteAddr}: ${request.requestURI}" }
        val r = UpnpStreamProcessor(dlna).processMessage(streamRequestMapper.map(request))
        return ResponseEntity(
            r.body,
            HttpHeaders().also { h -> r.headers.entries.forEach { h.add(it.key, it.value.joinToString { it }) } },
            HttpStatusCode.valueOf(r.operation.statusCode)
        )
    }

    companion object : KLogging()
}
