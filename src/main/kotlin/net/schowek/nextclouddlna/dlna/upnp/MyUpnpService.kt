package net.schowek.nextclouddlna.dlna.upnp

import org.jupnp.UpnpServiceConfiguration
import org.jupnp.UpnpServiceImpl
import org.jupnp.protocol.ProtocolFactory
import org.jupnp.registry.RegistryImpl
import org.springframework.stereotype.Component

@Component
class MyUpnpService(
    upnpServiceConfiguration: UpnpServiceConfiguration
) : UpnpServiceImpl(upnpServiceConfiguration) {
    override fun createRegistry(pf: ProtocolFactory) = RegistryImpl(this)
}
