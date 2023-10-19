package support

import net.schowek.nextclouddlna.NextcloudDLNAApp
import net.schowek.nextclouddlna.util.ServerInfoProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootContextLoader
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import support.beans.TestConfig

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT

@ContextConfiguration(loader = SpringBootContextLoader, classes = NextcloudDLNAApp.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles("integration")
@Import(TestConfig.class)
class IntegrationSpecification extends Specification {
    @Autowired
    private TestRestTemplate restTemplate

    TestRestTemplate restTemplate() {
        return restTemplate
    }

    @Autowired
    private ServerInfoProvider serverInfoProvider

    protected String urlWithPort(String uri = "") {
        return "http://localhost:" + serverInfoProvider.port + uri;
    }
}
