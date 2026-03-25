package com.playground.delivery.consumer.config

import com.fasterxml.jackson.core.JsonProcessingException
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaAdmin.NewTopics
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConsumerConfig(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    @Bean
    fun orderCompletedListenerContainerFactory(
        configurer: ConcurrentKafkaListenerContainerFactoryConfigurer,
        consumerFactory: ConsumerFactory<Any, Any>,
    ): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        configurer.configure(factory, consumerFactory)
        factory.setConcurrency(10)
        factory.setCommonErrorHandler(
            DefaultErrorHandler(
                DeadLetterPublishingRecoverer(kafkaTemplate),
                FixedBackOff(1000L, 2L),
            ).apply {
                addNotRetryableExceptions(JsonProcessingException::class.java)
            },
        )
        return factory
    }

    @Bean
    fun dltListenerContainerFactory(
        configurer: ConcurrentKafkaListenerContainerFactoryConfigurer,
        consumerFactory: ConsumerFactory<Any, Any>,
    ): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        configurer.configure(factory, consumerFactory)
        factory.setConcurrency(1)
        return factory
    }

    @Bean
    fun dltTopics(): NewTopics =
        NewTopics(
            TopicBuilder
                .name(KafkaConsumerTopic.ORDER_COMPLETED_DLT)
                .partitions(3)
                .replicas(1)
                .build(),
        )
}
