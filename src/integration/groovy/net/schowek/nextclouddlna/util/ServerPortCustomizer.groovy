package net.schowek.nextclouddlna.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.server.ConfigurableWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("integration")
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Value("\${random.int(9090,65535)}")
    int port

    int getPort() {
        return port
    }

    @Override
    void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(port);
    }
}