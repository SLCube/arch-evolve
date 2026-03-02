package com.playground.order.consumer.config

import com.playground.order.consumer.support.KafkaConsumerMdcInterceptor
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.RecordInterceptor

@Configuration
class KafkaConsumerConfig(
    private val kafkaConsumerMdcInterceptor: KafkaConsumerMdcInterceptor,
) {
    @Bean
    @Suppress("UNCHECKED_CAST")
    fun kafkaListenerContainerFactory(
        configurer: ConcurrentKafkaListenerContainerFactoryConfigurer,
        consumerFactory: ConsumerFactory<Any, Any>,
    ): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        configurer.configure(factory, consumerFactory)
        factory.setRecordInterceptor(kafkaConsumerMdcInterceptor as RecordInterceptor<Any, Any>)
        return factory
    }
}
