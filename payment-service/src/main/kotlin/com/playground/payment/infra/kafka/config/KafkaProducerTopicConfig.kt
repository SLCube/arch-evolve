package com.playground.payment.infra.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaProducerTopicConfig {
    @Bean
    fun paymentAuthorizedTopic(): NewTopic =
        TopicBuilder
            .name(KafkaProducerTopic.PAYMENT_AUTHORIZED)
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun paymentFailedTopic(): NewTopic =
        TopicBuilder
            .name(KafkaProducerTopic.PAYMENT_FAILED)
            .partitions(3)
            .replicas(1)
            .build()
}
