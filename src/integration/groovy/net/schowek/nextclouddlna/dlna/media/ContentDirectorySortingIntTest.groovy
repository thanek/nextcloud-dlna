package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.dlna.DlnaService
import net.schowek.nextclouddlna.dlna.MediaServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import support.UpnpAwareSpecification

class ContentDirectorySortingIntTest extends UpnpAwareSpecification {

    @Autowired
    private MediaServer mediaServer
    @Autowired
    private DlnaService dlnaService

    def uid

    def setup() {
        uid = mediaServer.serviceIdentifier
        dlnaService.start()
    }

    // Root containers: johndoe (id=2), janedoe (id=387), family folder (id=586)

    def "should sort containers by title ascending (+dc:title)"() {
        when:
        def didl = browse("0", "+dc:title")

        then:
        didl.containers.collect { it.title } == ["family folder", "janedoe", "johndoe"]
    }

    def "should sort by the default criteria when the client requests no sort"() {
        when: "a client that does not support sorting browses the root"
        def didl = browse("0", "")

        then: "containers come back in the configured default order (+dc:title), not in the content tree order"
        didl.containers.collect { it.title } == ["family folder", "janedoe", "johndoe"]
    }

    def "should sort containers by title descending (-dc:title)"() {
        when:
        def didl = browse("0", "-dc:title")

        then:
        didl.containers.collect { it.title } == ["johndoe", "janedoe", "family folder"]
    }

    // johndoe (node 2) items: id=13 Nextcloud intro.mp4 (mtime=1695737656), id=14 Nextcloud.png (mtime=1695737700)

    def "should sort items by title ascending (+dc:title)"() {
        when:
        def didl = browse("2", "+dc:title")

        then:
        // "nextcloud intro.mp4" < "nextcloud.png" because space (32) < dot (46)
        didl.items.collect { it.title } == ["Nextcloud intro.mp4", "Nextcloud.png"]
    }

    def "should sort items by title descending (-dc:title)"() {
        when:
        def didl = browse("2", "-dc:title")

        then:
        didl.items.collect { it.title } == ["Nextcloud.png", "Nextcloud intro.mp4"]
    }

    def "should sort items by date ascending (+dc:date)"() {
        when:
        def didl = browse("2", "+dc:date")

        then:
        // id=13 mtime=1695737656 (older), id=14 mtime=1695737700 (newer)
        didl.items.collect { it.id } == ["13", "14"]
    }

    def "should sort items by date descending (-dc:date)"() {
        when:
        def didl = browse("2", "-dc:date")

        then:
        // id=14 mtime=1695737700 (newer) comes first
        didl.items.collect { it.id } == ["14", "13"]
    }

    // johndoe (node 2) items: id=13 video/mp4 -> object.item.videoItem, id=14 image/png -> object.item.imageItem
    // "object.item.imageItem" < "object.item.videoItem" alphabetically

    def "should sort items by upnp:class ascending (+upnp:class) — imageItem before videoItem"() {
        when:
        def didl = browse("2", "+upnp:class")

        then:
        didl.items.collect { it.id } == ["14", "13"]
    }

    def "should sort items by upnp:class descending (-upnp:class) — videoItem before imageItem"() {
        when:
        def didl = browse("2", "-upnp:class")

        then:
        didl.items.collect { it.id } == ["13", "14"]
    }

    def "should keep containers before items with +upnp:class (folders before files)"() {
        when:
        def didl = browse("2", "+upnp:class")

        then:
        didl.containers.size() == 1
        didl.containers[0].title == "photos"
        didl.items.size() == 2
    }

    private browse(String nodeId, String sortCriteria) {
        def response = performBrowseWithSort(nodeId, "BrowseDirectChildren", sortCriteria)
        return extractDIDLFromResponse(response)
    }

    private ResponseEntity performBrowseWithSort(String nodeId, String browseFlag, String sortCriteria) {
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
                '            <SortCriteria>' + sortCriteria + '</SortCriteria>\n' +
                '        </u:Browse>\n' +
                '    </s:Body>\n' +
                '</s:Envelope>'
        def headers = new HttpHeaders([
                'content-type': 'text/xml; charset="utf-8"',
                'soapaction'  : '"urn:schemas-upnp-org:service:ContentDirectory:1#Browse"'
        ])
        HttpEntity<String> request = new HttpEntity<String>(requestBody, headers)
        return restTemplate().postForEntity(urlWithPort("/dev/$uid/svc/upnp-org/ContentDirectory/action"), request, String)
    }
}
