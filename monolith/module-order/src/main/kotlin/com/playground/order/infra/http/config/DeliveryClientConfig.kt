package com.playground.order.infra.http.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient

@Configuration
class DeliveryClientConfig {
    @Bean
    fun deliveryRestClient(
        builder: RestClient.Builder,
        @Value("\${delivery-service.base-url}") baseUrl: String,
    ): RestClient =
        builder
            .baseUrl(baseUrl)
            .requestFactory(JdkClientHttpRequestFactory(HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
            .build()
}
