package com.playground.payment.infra.external.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class PgClientConfig {
    @Bean
    fun pgRestClient(
        builder: RestClient.Builder,
        @Value("\${pg.base-url}") baseUrl: String,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .build()
}
