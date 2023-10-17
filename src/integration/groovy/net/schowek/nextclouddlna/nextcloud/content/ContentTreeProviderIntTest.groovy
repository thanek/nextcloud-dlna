package net.schowek.nextclouddlna.nextcloud.content

import net.schowek.nextclouddlna.nextcloud.db.AppConfigId
import net.schowek.nextclouddlna.nextcloud.db.AppConfigRepository
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll
import support.IntegrationSpecification

@Unroll
class ContentTreeProviderIntTest extends IntegrationSpecification {
    @Autowired
    ContentTreeProvider contentTreeProvider
    @Autowired
    AppConfigRepository appConfigRepository

    def "should create content tree including the group folder"() {
        when:
        contentTreeProvider.rebuildTree(true)

        then:
        def root = contentTreeProvider.getNode("0")
        with(root) {
            nodes.size() == 3
            nodes[0].name == "johndoe"
            nodes[1].name == "janedoe"
            nodes[2].name == "family folder"
        }
    }

    def "should create content tree without the group folder when the option is disabled"() {
        given:
        appConfigRepository.deleteById(new AppConfigId("groupfolders", "enabled"))

        when:
        contentTreeProvider.rebuildTree(true)

        then:
        def root = contentTreeProvider.getNode("0")
        with(root) {
            nodes.size() == 2
            nodes[0].name == "johndoe"
            nodes[1].name == "janedoe"
        }
    }
}
