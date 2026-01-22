package com.playground.product.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["spring.data.redis.host"])
@EnableConfigurationProperties(RedisProperties::class)
class RedissonConfig(
    private val redisProperties: RedisProperties,
) {
    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config.useSingleServer()
            .setAddress("redis://${redisProperties.host}:${redisProperties.port}")
            .setConnectionPoolSize(50)
            .setConnectionMinimumIdleSize(10)
            .setTimeout(3000)
            .setRetryAttempts(3)
            .setRetryInterval(1000)

        return Redisson.create(config)
    }
}