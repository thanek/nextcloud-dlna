package net.schowek.nextclouddlna.controller

import jakarta.servlet.http.HttpServletRequest
import mu.KLogging
import net.schowek.nextclouddlna.dlna.DlnaService
import net.schowek.nextclouddlna.dlna.StreamMessageMapper
import net.schowek.nextclouddlna.dlna.MediaServer
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.*
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController


@RestController
class UpnpController(
    private val streamMessageMapper: StreamMessageMapper,
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
        return streamMessageMapper.map(request).let { req ->
            dlnaService.processRequest(req).let { res ->
                streamMessageMapper.map(res)
            }
        }
    }

    companion object : KLogging()
}
