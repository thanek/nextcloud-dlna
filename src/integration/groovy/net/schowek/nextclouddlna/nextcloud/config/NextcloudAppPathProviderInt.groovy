package net.schowek.nextclouddlna.nextcloud.config


import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("integration")
class NextcloudAppPathProviderInt implements NextcloudAppPathProvider {
    @Override
    File getNextcloudDir() {
        return new File(getClass().getResource("/nextcloud/app/data").getFile())
    }
}
