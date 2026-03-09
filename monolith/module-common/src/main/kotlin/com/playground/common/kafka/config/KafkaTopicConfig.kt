package com.playground.common.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    @Bean
    fun orderCreatedTopic(): NewTopic =
        TopicBuilder
            .name(KafkaProducerTopic.ORDER_CREATED)
            .partitions(50)
            .replicas(1)
            .build()
}
