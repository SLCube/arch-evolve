package playground.common.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    @Bean
    fun orderCreatedTopic(): NewTopic =
        TopicBuilder
            .name(KafkaTopic.ORDER_CREATED)
            .partitions(1)
            .replicas(1)
            .build()
}
