package com.playground.payment.infra.external.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class PgClientConfig {
    @Bean
    fun pgRestClient(
        builder: RestClient.Builder,
        @Value("\${pg.base-url}") baseUrl: String,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .requestFactory(
                JdkClientHttpRequestFactory(
                    HttpClient
                        .newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(3))
                        .build(),
                ).apply { setReadTimeout(Duration.ofSeconds(5)) },
            ).build()
}
