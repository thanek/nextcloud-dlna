package net.schowek.nextclouddlna.nextcloud.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.stream.Stream


@Repository
interface FilecacheRepository : JpaRepository<Filecache, String> {
    fun findByParent(parent: Int): List<Filecache>
    fun findByPath(path: String): List<Filecache>
    fun findFirstByOrderByStorageMtimeDesc(): Filecache
    fun findFirstByPath(path: String): Filecache

    @Query(
        "SELECT f,m FROM Filecache f, Mount m " +
                "WHERE m.storageId = f.storage " +
                "  AND path = 'files'"
    )
    fun mainNodes(): List<Array<Any>>

    @Query(
        "SELECT f FROM Filecache f " +
                "WHERE path LIKE :path " +
                "  AND storage = :storage " +
                "  AND mimetype <> :folderMimeType " +
                "ORDER BY size DESC"
    )
    fun findThumbnails(
        @Param("path") path: String,
        @Param("storage") storage: Int,
        @Param("folderMimeType") folderMimeType: Int
    ): Stream<Filecache>
}


@Entity
@Table(name = "oc_filecache")
data class Filecache(
    @Id
    @field:Column(name = "fileid")
    val id: Int,
    val storage: Int,
    val path: String,
    val parent: Int,
    val name: String,
    val mimetype: Int,
    val size: Long,
    val mtime: Long,
    val storageMtime: Long
) {
    companion object {
        var FOLDER_MIME_TYPE = "httpd/unix-directory"
    }
}

@Entity
@Table(name = "oc_mounts")
data class Mount(
    @Id
    val id: Int,
    val storageId: Int,
    val rootId: Int,
    val userId: String
)


