package net.schowek.nextclouddlna.nextcloud.content

import net.schowek.nextclouddlna.nextcloud.db.AppConfigId
import net.schowek.nextclouddlna.nextcloud.db.AppConfigRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import spock.lang.Unroll
import support.IntegrationSpecification

@Unroll
@TestPropertySource(properties = [
        "nextcloud.scannedFolders=/johndoe,/family folder"
])
class FilteringContentTreeProviderIntTest extends IntegrationSpecification {
    @Autowired
    ContentTreeProvider contentTreeProvider
    @Autowired
    AppConfigRepository appConfigRepository

    def "should create content tree including only the folders declared for scanning"() {
        when:
        contentTreeProvider.rebuildTree(true)

        then:
        def root = contentTreeProvider.getNode("0")
        with(root) {
            nodes.size() == 2
            nodes[0].name == "johndoe"
            nodes[1].name == "family folder"
        }
        root.nodes.findAll { it.name == "janedoe" }.size() == 0
    }

    def "should create content tree from scannedFolders without the group folder when the option is disabled"() {
        given:
        appConfigRepository.deleteById(new AppConfigId("groupfolders", "enabled"))

        when:
        contentTreeProvider.rebuildTree(true)

        then:
        def root = contentTreeProvider.getNode("0")
        with(root) {
            nodes.size() == 1
            nodes[0].name == "johndoe"
        }
        root.nodes.findAll { it.name == "janedoe" }.size() == 0
    }
}
