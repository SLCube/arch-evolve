package com.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class PlayGroundApplication

fun main(args: Array<String>) {
    runApplication<PlayGroundApplication>(*args)
}
