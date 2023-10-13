package net.schowek.nextclouddlna.util

import org.springframework.stereotype.Component
import java.net.URI


@Component
class ExternalUrls(private val serverInfoProvider: ServerInfoProvider) {
    val selfUriString: String =
        "http://" + serverInfoProvider.address!!.hostAddress + ":" + serverInfoProvider.port

    val selfURI : URI get() = URI(selfUriString)

    fun contentUrl(id: Int) = "$selfUriString/c/$id"
}

