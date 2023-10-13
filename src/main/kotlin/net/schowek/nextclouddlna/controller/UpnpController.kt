package net.schowek.nextclouddlna.controller

import jakarta.servlet.http.HttpServletRequest
import mu.KLogging
import net.schowek.nextclouddlna.DlnaService
import net.schowek.nextclouddlna.dlna.StreamRequestMapper
import net.schowek.nextclouddlna.dlna.media.MediaServer
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.*
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController


@RestController
class UpnpController(
    private val streamRequestMapper: StreamRequestMapper,
    private val dlnaService: DlnaService
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
        logger.info { "GET icon request from ${request.remoteAddr}: ${request.requestURI}" }
        return InputStreamResource(MediaServer.iconResource());
    }

    @RequestMapping(
        method = [GET, HEAD, POST], value = [
            "/dev/{uid}/desc",
            "/dev/{uid}/svc/upnp-org/ContentDirectory/desc",
            "/dev/{uid}/svc/upnp-org/ConnectionManager/desc",
            "/dev/{uid}/svc/upnp-org/ContentDirectory/action"
        ],
        produces = ["application/xml;charset=utf8", "text/xml;charset=utf8"]
    )
    fun handleUpnp(
        @PathVariable("uid") uid: String,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        logger.info { "Upnp ${request.method} request from ${request.remoteAddr}: ${request.requestURI}" }
        return with(dlnaService.processRequest(streamRequestMapper.map(request))) {
            ResponseEntity(
                body,
                HttpHeaders().also { h -> headers.entries.forEach { e -> h.add(e.key, e.value.joinToString { it }) } },
                HttpStatusCode.valueOf(operation.statusCode)
            )
        }
    }

    companion object : KLogging()
}
