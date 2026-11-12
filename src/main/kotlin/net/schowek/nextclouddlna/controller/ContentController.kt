package net.schowek.nextclouddlna.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import net.schowek.nextclouddlna.dlna.media.DlnaProtocolInfoBuilder
import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class ContentController(
    private val contentTreeProvider: ContentTreeProvider,
    private val dlnaProtocolInfoBuilder: DlnaProtocolInfoBuilder
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
                response.addHeader("contentFeatures.dlna.org", dlnaProtocolInfoBuilder.build(item.format).toString())
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

    companion object : KLogging()
}

