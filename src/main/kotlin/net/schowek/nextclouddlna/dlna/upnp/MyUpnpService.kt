package net.schowek.nextclouddlna.dlna.upnp

import org.jupnp.UpnpServiceConfiguration
import org.jupnp.UpnpServiceImpl
import org.springframework.stereotype.Component

@Component
class MyUpnpService(
    upnpServiceConfiguration: UpnpServiceConfiguration
) : UpnpServiceImpl(upnpServiceConfiguration)
