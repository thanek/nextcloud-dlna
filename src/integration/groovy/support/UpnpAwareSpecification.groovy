package support

import org.jupnp.support.contentdirectory.DIDLParser
import org.jupnp.support.model.DIDLContent
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
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

class UpnpAwareSpecification extends IntegrationSpecification {
    Document createDocument(ResponseEntity<String> response) {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader(response.body))
        );
    }

    String nodeValue(Document dom, String pattern) {
        return node(dom, "$pattern/text()").nodeValue
    }

    Node node(Document dom, String pattern) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (xpath.evaluate(pattern, dom, NODE) as Node)
    }

    NodeList nodesList(Document dom, String pattern) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (xpath.evaluate(pattern, dom, NODESET) as NodeList)
    }

    ResponseEntity performContentDirectoryAction(String nodeId, String browseFlag) {
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

    DIDLContent extractDIDLFromResponse(ResponseEntity<String> response) {
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader(response.body))
        );
        return new DIDLParser().parse(nodeValue(dom, "/Envelope/Body/BrowseResponse/Result"))
    }
}
