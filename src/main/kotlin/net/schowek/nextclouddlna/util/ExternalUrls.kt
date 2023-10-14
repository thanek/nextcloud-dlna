package net.schowek.nextclouddlna.util

import org.springframework.stereotype.Component
import java.net.URI


@Component
class ExternalUrls(
    serverInfoProvider: ServerInfoProvider
) {
    val selfUriString: String =
        when (serverInfoProvider.port) {
            80 -> "http://${serverInfoProvider.host}"
            else -> "http://${serverInfoProvider.host}:${serverInfoProvider.port}"
        }


    val selfURI: URI get() = URI(selfUriString)

    fun contentUrl(id: Int) = "$selfUriString/c/$id"
}

