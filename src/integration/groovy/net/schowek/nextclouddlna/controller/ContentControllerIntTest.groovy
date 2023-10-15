package net.schowek.nextclouddlna.controller


import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import support.IntegrationSpecification

class ContentControllerIntTest extends IntegrationSpecification {

    def "should process GET request for content"() {
        when:
        ResponseEntity<byte[]> response = restTemplate().getForEntity(urlWithPort("/c/16"), byte[]);

        then:
        response.statusCode == HttpStatus.OK
        with(response.headers.each { it.key.toLowerCase() }) {
            assert it['content-type'] == ['image/jpeg']
            assert it['accept-ranges'] == ["bytes"]
            assert it.containsKey('contentfeatures.dlna.org')
            assert it.containsKey('transfermode.dlna.org')
            assert it.containsKey('realtimeinfo.dlna.org')
        }
        response.body.length == 593508
    }

    def "should return 404 if content does not exist"() {
        when:
        ResponseEntity<byte[]> response = restTemplate().getForEntity(urlWithPort("/c/blah-blah"), byte[]);

        then:
        response.statusCode == HttpStatus.NOT_FOUND
    }
}
