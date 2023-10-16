package support

import net.schowek.nextclouddlna.NextcloudDLNAApp
import net.schowek.nextclouddlna.util.ServerInfoProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT

@ContextConfiguration(loader = SpringBootContextLoader, classes = NextcloudDLNAApp.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles("integration")
class IntegrationSpecification extends Specification {
    private TestRestTemplate restTemplate = new TestRestTemplate()

    // TODO BEAN
    TestRestTemplate restTemplate() {
        if (restTemplate == null) {
            restTemplate = new TestRestTemplate()
        }
        return restTemplate
    }

    @Autowired
    private ServerInfoProvider serverInfoProvider

    def setup() {
        System.err.println("SETUP PARENT")
    }

    protected String urlWithPort(String uri = "") {
        return "http://localhost:" + serverInfoProvider.port + uri;
    }
}
