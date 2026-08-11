package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import org.jupnp.binding.annotations.AnnotationLocalServiceBinder
import org.jupnp.support.contentdirectory.DIDLParser
import org.jupnp.model.DefaultServiceManager
import org.jupnp.model.meta.LocalService
import org.jupnp.support.connectionmanager.ConnectionManagerService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class LocalServiceConfiguration {
    @Bean
    fun didlParser(): DIDLParser = DIDLParser()

    @Bean
    fun contentDirectoryService(
        contentTreeProvider: ContentTreeProvider,
        nodeConverter: NodeConverter,
        didlParser: DIDLParser,
        resultSorter: ResultSorter,
        browseResultBuilder: BrowseResultBuilder
    ) = ContentDirectoryService(contentTreeProvider, nodeConverter, didlParser, resultSorter, browseResultBuilder)

    @Bean
    @Qualifier("contentDirectoryLocalService")
    @Suppress("UNCHECKED_CAST")
    fun contentDirectoryLocalService(
        contentDirectoryService: ContentDirectoryService
    ): LocalService<ContentDirectoryService> {
        val localService =
            AnnotationLocalServiceBinder().read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>
        val manager = object : DefaultServiceManager<ContentDirectoryService>(localService, ContentDirectoryService::class.java) {
            override fun createServiceInstance(): ContentDirectoryService = contentDirectoryService

            // No-op: ContentDirectoryService delegates to ContentTreeProvider,
            // which is already thread-safe (@Volatile tree, @Synchronized rebuildTree).
            override fun lock() {}
            override fun unlock() {}
        }
        return localService.also { it.setManager(manager) }
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
