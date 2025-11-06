package com.playground

import com.playground.common.security.jwt.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
class PlayGroundApplication

fun main(args: Array<String>) {
	runApplication<PlayGroundApplication>(*args)
}
