package net.schowek.nextclouddlna.nextcloud.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface GroupFolderRepository : JpaRepository<GroupFolder, Int>

@Entity
@Table(name = "oc_group_folders")
data class GroupFolder(
    @Id
    @Column(name = "folder_id")
    val id: Int,

    @Column(name = "mount_point")
    val name: String
)

