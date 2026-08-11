package net.schowek.nextclouddlna.nextcloud.db

import org.springframework.stereotype.Component

@Component
class DefaultMimetypeResolver(
    mimetypeRepository: MimetypeRepository
) : MimetypeResolver {
    private val mimetypes: Map<Int, String> = mimetypeRepository.findAll().associate { it.id to it.mimetype }
    private val _folderMimeTypeId: Int = mimetypes.entries.find { it.value == Filecache.FOLDER_MIME_TYPE }?.key
        ?: error("Mimetype '${Filecache.FOLDER_MIME_TYPE}' not found in Nextcloud mimetypes table. Is the Nextcloud database configured correctly?")

    override fun resolve(mimetypeId: Int): String? = mimetypes[mimetypeId]
    override val folderMimeTypeId: Int get() = _folderMimeTypeId
}
