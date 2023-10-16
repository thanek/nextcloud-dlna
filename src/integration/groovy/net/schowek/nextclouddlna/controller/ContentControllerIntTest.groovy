package net.schowek.nextclouddlna.controller


import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import support.IntegrationSpecification

class ContentControllerIntTest extends IntegrationSpecification {
    @Autowired
    ContentTreeProvider contentTreeProvider

    def "should serve nextcloud files"() {
        given:
        def items = contentTreeProvider.tree.items

        when:
        Map<Integer, ResponseEntity<byte[]>> results = items.keySet().collectEntries() {
            [Integer.valueOf(it), restTemplate().getForEntity(urlWithPort("/c/$it"), byte[])]
        }

        then:
        items.values().each { item ->
            assert results.containsKey(item.id)
            with(results.get(item.id)) { response ->
                response.statusCode == HttpStatus.OK
                with(response.headers.each { it.key.toLowerCase() }) {
                    assert it['content-type'] == [item.format.mime]
                    assert it['accept-ranges'] == ["bytes"]
                    assert it.containsKey('contentfeatures.dlna.org')
                    assert it.containsKey('transfermode.dlna.org')
                    assert it.containsKey('realtimeinfo.dlna.org')
                }
                response.body.length == item.fileLength
            }
        }
    }

    def "should return 404 if content does not exist"() {
        when:
        ResponseEntity<byte[]> response = restTemplate().getForEntity(urlWithPort("/c/blah-blah"), byte[]);

        then:
        response.statusCode == HttpStatus.NOT_FOUND
    }
}
