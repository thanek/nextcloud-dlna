package net.schowek.nextclouddlna.dlna

import org.jupnp.UpnpService
import org.jupnp.model.message.discovery.OutgoingSearchRequest
import org.jupnp.model.message.header.HostHeader
import org.jupnp.model.message.header.MANHeader
import org.jupnp.model.message.header.STAllHeader
import org.jupnp.model.message.header.UpnpHeader
import org.jupnp.model.types.HostPort
import org.springframework.beans.factory.annotation.Autowired
import spock.util.concurrent.PollingConditions
import support.IntegrationSpecification
import support.beans.dlna.upnp.UpnpServiceConfigurationInt

import static org.jupnp.model.Constants.IPV4_UPNP_MULTICAST_GROUP
import static org.jupnp.model.Constants.UPNP_MULTICAST_PORT
import static org.jupnp.model.message.UpnpRequest.Method.MSEARCH
import static org.jupnp.model.message.header.UpnpHeader.Type.*
import static org.jupnp.model.types.NotificationSubtype.ALL
import static org.jupnp.model.types.NotificationSubtype.DISCOVER

class DlnaServiceIntTest extends IntegrationSpecification {
    @Autowired
    private UpnpService upnpService
    @Autowired
    private MediaServer mediaServer
    def conditions = new PollingConditions(timeout: 1)

    def "should send initial multicast Upnp datagrams on start"() {
        given:
        def configuration = upnpService.configuration as UpnpServiceConfigurationInt
        def sut = new DlnaService(upnpService, mediaServer)

        expect:
        configuration.outgoingDatagramMessages == []

        when:
        sut.start()

        then:
        conditions.eventually {
            assert configuration.outgoingDatagramMessages.any()
            assert configuration.outgoingDatagramMessages[0].class == OutgoingSearchRequest
            with(configuration.outgoingDatagramMessages[0] as OutgoingSearchRequest) {
                assert it.operation.method == MSEARCH
                assert it.destinationAddress == InetAddress.getByName(IPV4_UPNP_MULTICAST_GROUP)
                assert it.destinationPort == UPNP_MULTICAST_PORT

                assert header(it, MAN, MANHeader.class) == DISCOVER.headerString
                assert header(it, ST, STAllHeader.class).headerString == ALL.headerString
                assert header(it, HOST, HostHeader.class) == new HostPort(IPV4_UPNP_MULTICAST_GROUP, UPNP_MULTICAST_PORT)
            }
        }
    }

    def <T> T header(OutgoingSearchRequest request, UpnpHeader.Type type, Class<? extends UpnpHeader<T>> clazz) {
        return clazz.cast(request.headers.get(type).find()).value
    }
}
