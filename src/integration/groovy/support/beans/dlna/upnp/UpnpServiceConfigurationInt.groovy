package support.beans.dlna.upnp

import org.jupnp.DefaultUpnpServiceConfiguration
import org.jupnp.transport.spi.DatagramIO
import org.jupnp.transport.spi.NetworkAddressFactory
import org.jupnp.transport.spi.StreamClient
import org.jupnp.transport.spi.StreamServer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("integration")
class UpnpServiceConfigurationInt extends DefaultUpnpServiceConfiguration {
    @Override
    public StreamClient createStreamClient() {
        return null
    }


    @Override
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return null
    }

//    @Override
//    public DatagramIO createDatagramIO(NetworkAddressFactory networkAddressFactory) {
//
//    }
}
