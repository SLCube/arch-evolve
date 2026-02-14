package com.playground.auth.infra.adapter

import com.playground.auth.application.port.outbound.RefreshTokenPort
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisRefreshTokenAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
) : RefreshTokenPort {
    private fun getKey(userId: Long): String = "refresh_token:$userId"

    override fun save(
        userId: Long,
        refreshToken: String,
        ttl: Duration,
    ) {
        val key = getKey(userId)
        redisTemplate.opsForValue().set(key, refreshToken, ttl)
    }

    override fun findByUserId(userId: Long): String? {
        val key = getKey(userId)
        return redisTemplate.opsForValue().get(key)
    }

    override fun deleteByUserId(userId: Long) {
        val key = getKey(userId)
        redisTemplate.delete(key)
    }
}
