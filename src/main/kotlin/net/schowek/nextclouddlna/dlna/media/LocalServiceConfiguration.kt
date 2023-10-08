package net.schowek.nextclouddlna.dlna.media

import org.jupnp.binding.annotations.AnnotationLocalServiceBinder
import org.jupnp.model.DefaultServiceManager
import org.jupnp.model.meta.LocalService
import org.jupnp.support.connectionmanager.ConnectionManagerService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class LocalServiceConfiguration {
    @Bean
    @Qualifier("contentDirectoryLocalService")
    fun contentDirectoryLocalService(): LocalService<*> {
        return AnnotationLocalServiceBinder().read(ContentDirectoryService::class.java)
    }

    @Bean
    @Qualifier("connectionManagerLocalService")
    fun connectionManagerLocalService(): LocalService<*> {
        return AnnotationLocalServiceBinder().read(ConnectionManagerService::class.java)
    }

    @Bean
    fun connectionServiceManager(
        @Qualifier("connectionManagerLocalService")
        connectionManagerService: LocalService<ConnectionManagerService>
    ): DefaultServiceManager<ConnectionManagerService> {
        return DefaultServiceManager(
            connectionManagerService, ConnectionManagerService::class.java
        ).also { connectionManagerService.setManager(it) }
    }
}

