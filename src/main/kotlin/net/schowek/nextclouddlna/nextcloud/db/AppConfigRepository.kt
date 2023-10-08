package net.schowek.nextclouddlna.nextcloud.db

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AppConfigRepository : JpaRepository<AppConfig, AppConfigId> {
    @Query(
        "SELECT a.value " +
                "FROM AppConfig a " +
                "WHERE id.appId=:appId " +
                "  AND id.configKey=:configKey"
    )
    fun getValue(@Param("appId") appId: String, @Param("configKey") configKey: String): String?
}


@Entity
@Table(name = "oc_appconfig")
class AppConfig(
    @EmbeddedId
    val id: AppConfigId,
    @field:Column(name = "configvalue")
    private val value: String
)

@Embeddable
class AppConfigId(
    @Column(name = "appid")
    val appId: String,
    @Column(name = "configkey")
    val configKey: String
)

