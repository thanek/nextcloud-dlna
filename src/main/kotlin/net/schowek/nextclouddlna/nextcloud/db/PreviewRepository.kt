package net.schowek.nextclouddlna.nextcloud.db

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.stream.Stream


@Repository
interface PreviewRepository : JpaRepository<Preview, Long> {
    @Query(
        "SELECT p FROM Preview p " +
                "WHERE p.versionId = -1 " +
                "  AND p.locationId IS NULL"
    )
    fun findLocalUnversioned(): Stream<Preview>
}

@Entity
@Table(name = "oc_previews")
data class Preview(
    @Id
    val id: Long,
    val fileId: Long,
    val locationId: Long?,
    val width: Int,
    val height: Int,
    val mimetypeId: Int,
    val max: Boolean,
    val cropped: Boolean,
    val size: Long,
    val versionId: Long
)
