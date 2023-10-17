package net.schowek.nextclouddlna.util

import spock.lang.Specification

class ExternalUrlsTest extends Specification {
    def serverInfoProvider = Mock(ServerInfoProvider)

    def "should generate main url for the service"() {
        given:
        serverInfoProvider.getPort() >> port
        serverInfoProvider.getHost() >> host
        def sut = new ExternalUrls(serverInfoProvider)

        when:
        def mainUrl = sut.selfURI

        then:
        mainUrl.toString() == expectedUrl

        where:
        host      | port || expectedUrl
        "foo.bar" | 9999 || "http://foo.bar:9999"
        "foo.bar" | 80   || "http://foo.bar"
    }

    def "should generate content urls"() {
        given:
        serverInfoProvider.getPort() >> port
        serverInfoProvider.getHost() >> host
        def sut = new ExternalUrls(serverInfoProvider)

        when:
        def contentUrl = sut.contentUrl(contentId)

        then:
        contentUrl.toString() == expectedUrl

        where:
        host      | port | contentId || expectedUrl
        "foo.bar" | 9999 | 123       || "http://foo.bar:9999/c/123"
        "foo.bar" | 80   | 123       || "http://foo.bar/c/123"
    }
}
