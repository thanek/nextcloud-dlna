package net.schowek.nextclouddlna.util

import mu.KLogging
import net.schowek.nextclouddlna.util.NextcloudDBType.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
@Profile("!integration")
@EnableConfigurationProperties(NextcloudDBConfigProperties::class)
class DriverManagerDataSourceConfig {
    @Bean
    fun driverManagerDataSource(props: NextcloudDBConfigProperties): DriverManagerDataSource {
        logger.info { "Using Nextcloud DB connection parameters: $props" }
        return DriverManagerDataSource().also { dataSource ->
            when (props.type) {
                MARIADB, MYSQL -> {
                    dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
                    dataSource.url = "jdbc:mariadb://${props.host}:${props.port}/${props.name}";
                }

                POSTGRES -> {
                    dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
                    dataSource.url = "jdbc:postgresql://${props.host}:${props.port}/${props.name}";
                    dataSource.connectionProperties?.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                }

                else -> throw RuntimeException("Unsupported DB type")
            }
            dataSource.username = props.user;
            dataSource.password = props.pass;
        }
    }

    companion object : KLogging()
}

@Profile("!integration")
@ConfigurationProperties(prefix = "nextcloud.db")
data class NextcloudDBConfigProperties(
    val type: NextcloudDBType,
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val pass: String
)

enum class NextcloudDBType(val value: String) {
    MYSQL("mysql"),
    MARIADB("mariadb"),
    POSTGRES("postgres")
}