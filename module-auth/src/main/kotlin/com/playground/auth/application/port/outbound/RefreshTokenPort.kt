package com.playground.auth.application.port.outbound

import java.time.Duration

interface RefreshTokenPort {
    fun save(
        userId: Long,
        refreshToken: String,
        ttl: Duration,
    )

    fun findByUserId(userId: Long): String?

    fun deleteByUserId(userId: Long)
}
