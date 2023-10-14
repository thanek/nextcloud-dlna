package net.schowek.nextclouddlna.controller

import net.schowek.nextclouddlna.dlna.media.MediaServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import support.IntegrationSpecification

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

import static javax.xml.xpath.XPathConstants.NODE

class UpnpControllerIntTest extends IntegrationSpecification {

    @Autowired
    private MediaServer mediaServer

    def "should serve icon"() {
        given:
        def uid = mediaServer.serviceIdentifier

        when:
        ResponseEntity<byte[]> response = restTemplate().getForEntity(urlWithPort("/dev/${uid}/icon.png"), byte[]);

        then:
        response.statusCode == HttpStatus.OK
        with(response.headers.each { it.key.toLowerCase() }) {
            assert it['content-type'] == ['application/octet-stream']
        }
    }

    def "should serve service descriptor"() {
        given:
        def uid = mediaServer.serviceIdentifier

        when:
        ResponseEntity<String> response = restTemplate().getForEntity(urlWithPort("/dev/${uid}/desc"), String);

        then:
        response.statusCode == HttpStatus.OK
        with(response.headers.each { it.key.toLowerCase() }) {
            assert it['content-type'] == ['text/xml']
        }

        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader(response.body))
        );

        then:
        nodeValue(dom, "/root/device/friendlyName") == "nextcloud-dlna-int-test"
        nodeValue(dom, "/root/device/UDN") == "uuid:${uid}"
        nodeValue(dom, "/root/device/presentationURL") == urlWithPort()
    }

    private String nodeValue(Document dom, String pattern) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (xpath.evaluate("$pattern/text()", dom, NODE) as Node).nodeValue
    }
}
