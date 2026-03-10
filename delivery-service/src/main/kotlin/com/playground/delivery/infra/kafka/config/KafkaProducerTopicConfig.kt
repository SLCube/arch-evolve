package com.playground.delivery.infra.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaProducerTopicConfig {
    @Bean
    fun deliveryCreatedTopic(): NewTopic =
        TopicBuilder
            .name(KafkaProducerTopic.DELIVERY_CREATED)
            .partitions(50)
            .replicas(1)
            .build()
}
