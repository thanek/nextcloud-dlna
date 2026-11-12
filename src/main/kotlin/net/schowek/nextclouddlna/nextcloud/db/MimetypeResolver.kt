package net.schowek.nextclouddlna.nextcloud.db

interface MimetypeResolver {
    fun resolve(mimetypeId: Int): String?
    val folderMimeTypeId: Int
}
