package net.schowek.nextclouddlna.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.schowek.nextclouddlna.nextcloud.content.ContentGroup.*
import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import org.jupnp.support.model.Protocol
import org.jupnp.support.model.ProtocolInfo
import org.jupnp.support.model.dlna.*
import org.jupnp.support.model.dlna.DLNAAttribute.Type
import org.jupnp.support.model.dlna.DLNAAttribute.Type.*
import org.jupnp.support.model.dlna.DLNAConversionIndicator.NONE
import org.jupnp.support.model.dlna.DLNAFlags.*
import org.jupnp.support.model.dlna.DLNAOperations.*
import org.jupnp.support.model.dlna.DLNAProfiles.*
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
class ContentController(
    private val contentTreeProvider: ContentTreeProvider
) {
    final val logger = LoggerFactory.getLogger(ContentController::class.java)

    @RequestMapping(method = [RequestMethod.GET, RequestMethod.HEAD], value = ["/c/{id}"])
    @ResponseBody
    fun getResource(
        @PathVariable("id") id: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<FileSystemResource> {
        val item = contentTreeProvider.getItem(id)
        if (item == null) {
            logger.info("Could not find item id: {}", id)
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        if (!request.getHeaders("range").hasMoreElements()) {
            logger.info("Serving content {} {}", request.method, id)
        }
        val fileSystemResource = FileSystemResource(item.path)
        response.addHeader("Content-Type", item.format.mime)
        response.addHeader("contentFeatures.dlna.org", makeProtocolInfo(item.format).toString())
        response.addHeader("transferMode.dlna.org", "Streaming")
        response.addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*")
        return ResponseEntity(fileSystemResource, HttpStatus.OK)
    }

    @RequestMapping(method = [RequestMethod.GET], value = ["/rebuild"])
    @ResponseBody
    fun reloadTree(): ResponseEntity<*> {
        contentTreeProvider.rebuildTree()
        return ResponseEntity<Any>(HttpStatus.OK)
    }

    private fun makeProtocolInfo(mediaFormat: MediaFormat): DLNAProtocolInfo {
        val attributes = EnumMap<Type, DLNAAttribute<*>>(
            Type::class.java
        )
        if (mediaFormat.contentGroup === VIDEO) {
            attributes[DLNA_ORG_PN] = DLNAProfileAttribute(AVC_MP4_LPCM)
            attributes[DLNA_ORG_OP] = DLNAOperationsAttribute(RANGE)
            attributes[DLNA_ORG_CI] = DLNAConversionIndicatorAttribute(NONE)
            attributes[DLNA_ORG_FLAGS] = DLNAFlagsAttribute(
                INTERACTIVE_TRANSFERT_MODE,
                BACKGROUND_TRANSFERT_MODE,
                DLNA_V15,
                STREAMING_TRANSFER_MODE
            )
        }
        return DLNAProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, mediaFormat.mime, attributes)
    }
}

