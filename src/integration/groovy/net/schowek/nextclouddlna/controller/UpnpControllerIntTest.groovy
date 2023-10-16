package net.schowek.nextclouddlna.controller

import net.schowek.nextclouddlna.DlnaService
import net.schowek.nextclouddlna.dlna.media.MediaServer
import org.jupnp.support.contentdirectory.DIDLParser
import org.jupnp.support.model.DIDLObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import support.IntegrationSpecification

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

import static javax.xml.xpath.XPathConstants.NODE
import static javax.xml.xpath.XPathConstants.NODESET
import static org.jupnp.support.model.WriteStatus.NOT_WRITABLE

class UpnpControllerIntTest extends IntegrationSpecification {

    @Autowired
    private MediaServer mediaServer
    @Autowired
    private DlnaService dlnaService

    def uid

    def setup() {
        uid = mediaServer.serviceIdentifier
    }

    def "should serve icon"() {
        when:
        def response = restTemplate().getForEntity(urlWithPort("/dev/${uid}/icon.png"), byte[]);

        then:
        response.statusCode == HttpStatus.OK
        with(response.headers.each { it.key.toLowerCase() }) {
            assert it['content-type'] == ['application/octet-stream']
        }
    }

    def "should serve service descriptor"() {
        when:
        def response = restTemplate().getForEntity(urlWithPort("/dev/${uid}/desc"), String);

        then:
        response.statusCode == HttpStatus.OK
        with(response.headers.each { it.key.toLowerCase() }) {
            assert it['content-type'] == ['text/xml']
        }

        when:
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader(response.body))
        );

        then:
        nodeValue(dom, "/root/device/friendlyName") == "nextcloud-dlna-int-test"
        nodeValue(dom, "/root/device/UDN") == "uuid:${uid}"
        nodeValue(dom, "/root/device/presentationURL") == urlWithPort()
    }

    def "should serve content directory desc"() {
        when:
        def response = restTemplate().getForEntity(urlWithPort("/dev/${uid}/svc/upnp-org/ContentDirectory/desc"), String);

        then:
        response.statusCode == HttpStatus.OK
        with(response.headers.each { it.key.toLowerCase() }) {
            assert it['content-type'] == ['text/xml']
        }

        when:
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader(response.body))
        );

        then:
        with(node(dom, "/scpd/serviceStateTable/stateVariable[name='A_ARG_TYPE_BrowseFlag']/allowedValueList").childNodes) {
            assert it.length == 2
            assert it.item(0).textContent == "BrowseMetadata"
            assert it.item(1).textContent == "BrowseDirectChildren"
        }
    }

    def "should serve connectionMgr desc"() {
        when:
        def response = restTemplate().getForEntity(urlWithPort("/dev/${uid}/svc/upnp-org/ConnectionManager/desc"), String);

        then:
        response.statusCode == HttpStatus.OK
        with(response.headers.each { it.key.toLowerCase() }) {
            assert it['content-type'] == ['text/xml']
        }

        when:
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader(response.body))
        );

        then:
        with(nodeList(dom, "/scpd/actionList/action/name")) {
            assert it.length == 3
            it.item(0).textContent == "GetCurrentConnectionIDs"
            it.item(1).textContent == "GetProtocolInfo"
            it.item(2).textContent == "GetCurrentConnectionInfo"
        }
    }

    def "should handle upnp browse ROOT request"() {
        given:
        def requestBody = '<?xml version="1.0" encoding="utf-8"?>\n' +
                '<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"\n' +
                '            s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">\n' +
                '    <s:Body>\n' +
                '        <u:Browse xmlns:u="urn:schemas-upnp-org:service:ContentDirectory:1">\n' +
                '            <ObjectID>0</ObjectID>\n' +
                '            <BrowseFlag>BrowseMetadata</BrowseFlag>\n' +
                '            <Filter>*</Filter>\n' +
                '            <StartingIndex>0</StartingIndex>\n' +
                '            <RequestedCount>0</RequestedCount>\n' +
                '            <SortCriteria></SortCriteria>\n' +
                '        </u:Browse>\n' +
                '    </s:Body>\n' +
                '</s:Envelope>'

        when:
        def headers = new HttpHeaders([
                'content-type': 'text/xml; charset="utf-8"',
                'soapaction'  : '"urn:schemas-upnp-org:service:ContentDirectory:1#Browse"'
        ]);
        HttpEntity<String> request = new HttpEntity<String>(requestBody, headers);
        def response = restTemplate().postForEntity(urlWithPort("/dev/$uid/svc/upnp-org/ContentDirectory/action"), request, String)

        then:
        response.statusCode == HttpStatus.OK
        response.headers['content-type'].find() == "text/xml;charset=utf-8"

        when:
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader(response.body))
        );
        def didl = new DIDLParser().parse(nodeValue(dom, "/Envelope/Body/BrowseResponse/Result"))

        then:
        didl.containers.size() == 1
        with(didl.containers[0]) {
            assert id == "0"
            assert parentID == "-1"
            assert searchable
            assert restricted
            assert title == "ROOT"
            assert writeStatus == NOT_WRITABLE
            assert clazz.value == new DIDLObject.Class("object.container").value
            assert childCount == 3 // johndoe, janedoe, family folder
        }
    }

    private String nodeValue(Document dom, String pattern) {
        return node(dom, "$pattern/text()").nodeValue
    }

    private Node node(Document dom, String pattern) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (xpath.evaluate(pattern, dom, NODE) as Node)
    }

    private NodeList nodeList(Document dom, String pattern) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (xpath.evaluate(pattern, dom, NODESET) as NodeList)
    }
}
