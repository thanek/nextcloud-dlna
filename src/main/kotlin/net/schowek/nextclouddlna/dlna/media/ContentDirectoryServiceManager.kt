package net.schowek.nextclouddlna.dlna.media

import org.jupnp.model.DefaultServiceManager
import org.jupnp.model.meta.LocalService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component


@Component
class ContentDirectoryServiceManager(
    @Qualifier("contentDirectoryLocalService")
    private val service: LocalService<ContentDirectoryService>,
    private val contentDirectoryService: ContentDirectoryService
) : DefaultServiceManager<ContentDirectoryService>(service, ContentDirectoryService::class.java) {
    init {
        super.service.manager = this
    }

    override fun createServiceInstance(): ContentDirectoryService {
        return contentDirectoryService
    }

    // No-op: ContentDirectoryService delegates to ContentTreeProvider,
    // which is already thread-safe (@Volatile tree, @Synchronized rebuildTree).
    override fun lock() {}
    override fun unlock() {}
}

