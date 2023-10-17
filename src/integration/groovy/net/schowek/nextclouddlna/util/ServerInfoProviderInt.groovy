package net.schowek.nextclouddlna.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("integration")
class ServerInfoProviderInt implements ServerInfoProvider {
    private final ServerPortCustomizer serverPortCustomizer

    @Autowired
    ServerInfoProviderInt(ServerPortCustomizer serverPortCustomizer) {
        this.serverPortCustomizer = serverPortCustomizer
    }

    @Override
    String getHost() {
        return "localhost"
    }

    @Override
    int getPort() {
        return serverPortCustomizer.port
    }
}
