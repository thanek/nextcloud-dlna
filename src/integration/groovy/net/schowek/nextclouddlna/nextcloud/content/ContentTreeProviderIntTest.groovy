package net.schowek.nextclouddlna.nextcloud.content

import org.springframework.beans.factory.annotation.Autowired
import support.IntegrationSpecification

class ContentTreeProviderIntTest extends IntegrationSpecification {
    @Autowired
    ContentTreeProvider contentTreeProvider

    def "should foo"() {
        when:
        def result = contentTreeProvider.getItem("19")

        then:
        result.id == 19
    }
}
