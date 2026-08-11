package net.schowek.nextclouddlna.nextcloud.db

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class DefaultStorageUserMapper : StorageUserMapper {
    private val map: MutableMap<Int, String> = ConcurrentHashMap()

    override fun map(storageId: Int, userId: String) {
        map[storageId] = userId
    }

    override fun getUsername(storageId: Int): String? = map[storageId]
}
