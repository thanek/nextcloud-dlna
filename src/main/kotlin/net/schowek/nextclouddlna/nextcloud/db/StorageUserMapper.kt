package net.schowek.nextclouddlna.nextcloud.db

interface StorageUserMapper {
    fun map(storageId: Int, userId: String)
    fun getUsername(storageId: Int): String?
}
