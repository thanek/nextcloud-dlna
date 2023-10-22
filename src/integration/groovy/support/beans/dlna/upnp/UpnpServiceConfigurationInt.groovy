package support.beans.dlna.upnp

import org.jupnp.DefaultUpnpServiceConfiguration
import org.jupnp.model.message.OutgoingDatagramMessage
import org.jupnp.transport.impl.DatagramIOConfigurationImpl
import org.jupnp.transport.impl.DatagramIOImpl
import org.jupnp.transport.spi.DatagramIO
import org.jupnp.transport.spi.NetworkAddressFactory
import org.jupnp.transport.spi.StreamClient
import org.jupnp.transport.spi.StreamServer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("integration")
class UpnpServiceConfigurationInt extends DefaultUpnpServiceConfiguration {
    List<OutgoingDatagramMessage> outgoingDatagramMessages = new ArrayList<>()

    @Override
    public StreamClient createStreamClient() {
        return null
    }

    @Override
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return null
    }

    @Override
    public DatagramIO createDatagramIO(NetworkAddressFactory networkAddressFactory) {
        return new MockDatagramIO(this, new DatagramIOConfigurationImpl())
    }

    private void onOutgoingDatagramMessage(OutgoingDatagramMessage message) {
        outgoingDatagramMessages.add(message)
    }

    class MockDatagramIO extends DatagramIOImpl {
        private final UpnpServiceConfigurationInt upnpServiceConfiguration

        MockDatagramIO(UpnpServiceConfigurationInt upnpServiceConfiguration, DatagramIOConfigurationImpl configuration) {
            super(configuration)
            this.upnpServiceConfiguration = upnpServiceConfiguration
        }

        @Override
        void send(OutgoingDatagramMessage message) {
            upnpServiceConfiguration.onOutgoingDatagramMessage(message)
        }
    }
}
