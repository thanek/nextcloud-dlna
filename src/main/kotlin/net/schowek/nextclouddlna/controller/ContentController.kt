package net.schowek.nextclouddlna.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
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
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
class ContentController(
    private val contentTreeProvider: ContentTreeProvider
) {
    @RequestMapping(method = [RequestMethod.GET, RequestMethod.HEAD], value = ["/c/{id}"])
    @ResponseBody
    fun getResource(
        @PathVariable("id") id: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<FileSystemResource> {
        return contentTreeProvider.getItem(id)?.let { item ->
            if (!request.getHeaders("range").hasMoreElements()) {
                logger.info("Serving content ${request.method} $id")
            }
            val fileSystemResource = FileSystemResource(item.path)
            if (!fileSystemResource.exists()) {
                logger.info("Could not find file ${fileSystemResource.path} for item id: $id")
                ResponseEntity(HttpStatus.NOT_FOUND)
            } else {
                response.addHeader("Content-Type", item.format.mime)
                response.addHeader("contentFeatures.dlna.org", makeProtocolInfo(item.format).toString())
                response.addHeader("transferMode.dlna.org", "Streaming")
                response.addHeader("realTimeInfo.dlna.org", "DLNA.ORG_TLAG=*")
                ResponseEntity(fileSystemResource, HttpStatus.OK)
            }
        } ?: let {
            logger.info("Could not find item id: $id")
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(method = [RequestMethod.GET], value = ["/rebuild"])
    @ResponseBody
    fun reloadTree(): ResponseEntity<*> {
        contentTreeProvider.rebuildTree(true)
        return ResponseEntity<Any>(HttpStatus.OK)
    }

    private fun makeProtocolInfo(mediaFormat: MediaFormat): DLNAProtocolInfo {
        val attributes = EnumMap<Type, DLNAAttribute<*>>(Type::class.java)
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

    companion object : KLogging()
}

