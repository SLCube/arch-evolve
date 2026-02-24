package com.playground.payment.infra.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {
    @Bean
    fun paymentAuthorizedTopic(): NewTopic =
        TopicBuilder
            .name(KafkaTopic.PAYMENT_AUTHORIZED)
            .partitions(1)
            .replicas(1)
            .build()

    @Bean
    fun paymentFailedTopic(): NewTopic =
        TopicBuilder
            .name(KafkaTopic.PAYMENT_FAILED)
            .partitions(1)
            .replicas(1)
            .build()
}
