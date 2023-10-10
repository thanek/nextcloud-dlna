package net.schowek.nextclouddlna.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import net.schowek.nextclouddlna.NextcloudDLNA
import org.jupnp.model.message.Connection
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.HEAD
import org.springframework.web.bind.annotation.RequestMethod.POST
import org.springframework.web.bind.annotation.ResponseBody
import java.net.InetAddress

@Controller
class DLNAController(
    private val dlna: NextcloudDLNA
) {
    @RequestMapping(
        method = [GET, HEAD], value = [
            "/dev/{uid}/desc",
            "/dev/{uid}/svc/upnp-org/ContentDirectory/desc",
            "/dev/{uid}/svc/upnp-org/ConnectionManager/desc"
        ]
    )
    @ResponseBody
    fun handleGet(
        @PathVariable("uid") uid: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<String> {
        logger.info { "GET request from ${request.remoteAddr}: ${request.requestURI}" }
        MyUpnpStream(dlna.upnpService.protocolFactory, request, response).run()
        return ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
        method = [POST], value = [
            "/dev/{uid}/svc/upnp-org/ContentDirectory/action"
        ]
    )
    @ResponseBody
    fun handlePost(
        @PathVariable("uid") uid: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<String> {
        logger.info { "POST request from ${request.remoteAddr}: ${request.requestURI}" }
        MyUpnpStream(dlna.upnpService.protocolFactory, request, response).run()
        return ResponseEntity(HttpStatus.OK);
    }

    companion object : KLogging()
}

class MyHttpServerConnection(
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
