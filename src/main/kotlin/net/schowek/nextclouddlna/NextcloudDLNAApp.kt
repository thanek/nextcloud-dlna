package net.schowek.nextclouddlna

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableWebMvc
@EnableScheduling
class NextcloudDLNAApp

fun main(args: Array<String>) {
	runApplication<NextcloudDLNAApp>(*args)
}
