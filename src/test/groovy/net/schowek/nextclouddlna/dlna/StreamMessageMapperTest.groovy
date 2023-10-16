package net.schowek.nextclouddlna.dlna

import org.jupnp.model.message.StreamResponseMessage
import org.jupnp.model.message.UpnpHeaders
import org.jupnp.model.message.UpnpRequest
import org.jupnp.model.message.UpnpResponse
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

class StreamMessageMapperTest extends Specification {
    def sut = new StreamMessageMapper()

    def "should map servlet request to streamRequestMessage"() {
        given:
        def uri = "http://foo.bar/baz"
        def content = "some content"
        def headers = [
                "foo": "bar",
                "baz": "blah"
        ]

        def request = new MockHttpServletRequest(method, uri)
        request.setContent(content.getBytes("UTF-8"))
        headers.entrySet().forEach { request.addHeader(it.key, it.value) }

        when:
        def result = sut.map(request)

        then:
        result.uri == new URI(uri)
        result.operation.method == expectedMethod
        result.body.toString() == content
        result.headers.each {
            assert it.key.toLowerCase() in headers.keySet()
            assert it.value == [headers[it.key.toLowerCase()]]
        }

        where:
        method     || expectedMethod
        "GET"      || UpnpRequest.Method.GET
        "POST"     || UpnpRequest.Method.POST
        "M-SEARCH" || UpnpRequest.Method.MSEARCH
        "NOTIFY"   || UpnpRequest.Method.NOTIFY
    }


    def "should throw exception when http method is missing or not supported"() {
        given:
        def request = new MockHttpServletRequest(method, "http://foo.bar/")

        when:
        sut.map(request)

        then:
        thrown RuntimeException

        where:
        method | _
        null   | _
        "HEAD" | _
        "foo"  | _
    }

    def "should map streamResponseMessage to ResponseEntity"() {
        given:
        def content = "some content"
        def headers = [
                "foo": ["bar"],
                "baz": ["blah"]
        ]

        def response = new StreamResponseMessage(new UpnpResponse(responseStatus, "OK"))
        response.headers = new UpnpHeaders(headers)
        response.body = content

        when:
        def result = sut.map(response)

        then:
        result.statusCode == expectedHttpStatus
        result.body == content
        result.headers.each {
            assert headers.keySet().contains(it.key.toLowerCase())
            assert headers[it.key.toLowerCase()] == it.value
        }

        where:
        responseStatus || expectedHttpStatus
        200            || HttpStatus.OK
        404            || HttpStatus.NOT_FOUND
        500            || HttpStatus.INTERNAL_SERVER_ERROR
        400            || HttpStatus.BAD_REQUEST
    }

}
