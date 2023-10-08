package net.schowek.nextclouddlna.nextcloud.db

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface MimetypeRepository : JpaRepository<Mimetype, Int>

@Entity
@Table(name = "oc_mimetypes")
class Mimetype(
    @Id
    val id: Int,
    val mimetype: String
)

