package support.beans.nextcloud.config

import net.schowek.nextclouddlna.nextcloud.config.NextcloudAppPathProvider
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
