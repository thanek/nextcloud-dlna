package net.schowek.nextclouddlna.dlna


import org.jupnp.model.message.UpnpRequest
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

class StreamRequestMapperTest extends Specification {
    def sut = new StreamRequestMapper()

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


}
