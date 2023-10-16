package net.schowek.nextclouddlna.controller

import net.schowek.nextclouddlna.dlna.media.MediaServer
import org.jupnp.support.contentdirectory.DIDLParser
import org.jupnp.support.model.DIDLContent
import org.jupnp.support.model.DIDLObject
import org.jupnp.support.model.Protocol
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
import static org.jupnp.support.model.Protocol.HTTP_GET
import static org.jupnp.support.model.WriteStatus.NOT_WRITABLE

class UpnpControllerIntTest extends IntegrationSpecification {

    @Autowired
    private MediaServer mediaServer

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

    def "should handle upnp browse metadata for ROOT request"() {
        given:
        def nodeId = "0"
        def browseFlag = "BrowseMetadata"

        when:
        def response = performContentDirectoryAction(nodeId, browseFlag)

        then:
        response.statusCode == HttpStatus.OK
        response.headers['content-type'].find() == "text/xml;charset=utf-8"

        when:
        def didl = extractDIDLFromResponse(response)

        then:
        didl.containers.size() == 1
        didl.items.size() == 0
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

    def "should handle upnp browse direct children for ROOT request"() {
        given:
        def nodeId = "0"
        def browseFlag = "BrowseDirectChildren"

        when:
        def response = performContentDirectoryAction(nodeId, browseFlag)

        then:
        response.statusCode == HttpStatus.OK
        response.headers['content-type'].find() == "text/xml;charset=utf-8"

        when:
        def didl = extractDIDLFromResponse(response)

        then:
        didl.containers.size() == 3
        didl.items.size() == 0
        didl.containers.each {
            assert it.searchable
            assert it.restricted
            assert it.writeStatus == NOT_WRITABLE
            assert it.clazz.value == new DIDLObject.Class("object.container").value
        }

        with(didl.containers[0]) {
            assert id == "2"
            assert parentID == "1"
            assert title == "johndoe"
            assert childCount == 3
        }

        with(didl.containers[1]) {
            assert id == "387"
            assert parentID == "384"
            assert title == "janedoe"
            assert childCount == 1
        }

        with(didl.containers[2]) {
            assert id == "586"
            assert parentID == "584"
            assert title == "family folder"
            assert childCount == 1
        }
    }

    def "should handle upnp browse metadata for johndoe's directory request"() {
        given:
        def nodeId = "2"
        def browseFlag = "BrowseMetadata"

        when:
        def response = performContentDirectoryAction(nodeId, browseFlag)

        then:
        response.statusCode == HttpStatus.OK
        response.headers['content-type'].find() == "text/xml;charset=utf-8"

        when:
        def didl = extractDIDLFromResponse(response)

        then:
        didl.containers.size() == 1
        didl.items.size() == 0
        with(didl.containers[0]) {
            assert id == "2"
            assert parentID == "1"
            assert searchable
            assert restricted
            assert title == "johndoe"
            assert writeStatus == NOT_WRITABLE
            assert clazz.value == new DIDLObject.Class("object.container").value
            assert childCount == 3
        }
    }

    def "should handle upnp browse direct children for johndoe's directory request"() {
        given:
        def nodeId = "2"
        def browseFlag = "BrowseDirectChildren"

        when:
        def response = performContentDirectoryAction(nodeId, browseFlag)

        then:
        response.statusCode == HttpStatus.OK
        response.headers['content-type'].find() == "text/xml;charset=utf-8"

        when:
        def didl = extractDIDLFromResponse(response)

        then:
        didl.containers.size() == 1
        didl.items.size() == 2

        with(didl.containers[0]) {
            assert id == "15"
            assert parentID == "2"
            assert title == "photos"
            assert childCount == 2
            assert searchable
            assert restricted
            assert writeStatus == NOT_WRITABLE
            assert clazz.value == new DIDLObject.Class("object.container").value
        }

        with(didl.items[0]) {
            assert it.id == "13"
            assert it.parentID == "2"
            assert title == "Nextcloud intro.mp4"
            assert !restricted

            with(resources[0]) {
                assert protocolInfo.contentFormat == "video/mp4"
                assert protocolInfo.protocol == HTTP_GET
                assert size == 3963036
                assert value == urlWithPort("/c/13")
            }

            with(resources[1]) { thumbnail ->
                assert thumbnail.protocolInfo.contentFormat == "image/jpeg"
                assert thumbnail.protocolInfo.protocol == HTTP_GET
                assert thumbnail.protocolInfo.additionalInfo == "DLNA.ORG_PN=JPEG_TN"
                assert thumbnail.size == 28820
                assert thumbnail.value == urlWithPort("/c/273")
            }
        }

        with(didl.items[1]) {
            assert it.id == "14"
            assert it.parentID == "2"
            assert title == "Nextcloud.png"
            assert !restricted

            with(resources[0]) {
                assert protocolInfo.contentFormat == "image/png"
                assert protocolInfo.protocol == HTTP_GET
                assert size == 50598
                assert value == urlWithPort("/c/14")
            }

            with(resources[1]) { thumbnail ->
                assert thumbnail.protocolInfo.contentFormat == "image/png"
                assert thumbnail.protocolInfo.protocol == HTTP_GET
                assert thumbnail.protocolInfo.additionalInfo == "DLNA.ORG_PN=PNG_TN"
                assert thumbnail.size == 50545
                assert thumbnail.value == urlWithPort("/c/164")
            }
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

    private ResponseEntity performContentDirectoryAction(String nodeId, String browseFlag) {
        def requestBody = '<?xml version="1.0" encoding="utf-8"?>\n' +
                '<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/"\n' +
                '            s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">\n' +
                '    <s:Body>\n' +
                '        <u:Browse xmlns:u="urn:schemas-upnp-org:service:ContentDirectory:1">\n' +
                '            <ObjectID>' + nodeId + '</ObjectID>\n' +
                '            <BrowseFlag>' + browseFlag + '</BrowseFlag>\n' +
                '            <Filter>*</Filter>\n' +
                '            <StartingIndex>0</StartingIndex>\n' +
                '            <RequestedCount>200</RequestedCount>\n' +
                '            <SortCriteria></SortCriteria>\n' +
                '        </u:Browse>\n' +
                '    </s:Body>\n' +
                '</s:Envelope>'
        def headers = new HttpHeaders([
                'content-type': 'text/xml; charset="utf-8"',
                'soapaction'  : '"urn:schemas-upnp-org:service:ContentDirectory:1#Browse"'
        ]);
        HttpEntity<String> request = new HttpEntity<String>(requestBody, headers);
        return restTemplate().postForEntity(urlWithPort("/dev/$uid/svc/upnp-org/ContentDirectory/action"), request, String)
    }

    private DIDLContent extractDIDLFromResponse(ResponseEntity<String> response) {
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader(response.body))
        );
        return new DIDLParser().parse(nodeValue(dom, "/Envelope/Body/BrowseResponse/Result"))
    }
}
