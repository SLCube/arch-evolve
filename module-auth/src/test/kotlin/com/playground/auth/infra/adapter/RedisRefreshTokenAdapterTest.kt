package com.playground.auth.infra.adapter

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@Suppress("NonAsciiCharacters")
@DataRedisTest
@Testcontainers
class RedisRefreshTokenAdapterTest {

    companion object {
        @Container
        private val redisContainer = GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)

        @JvmStatic
        @DynamicPropertySource
        fun registerRedisProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    private lateinit var adapter: RedisRefreshTokenAdapter

    @BeforeEach
    fun setUp() {
        adapter = RedisRefreshTokenAdapter(redisTemplate)
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.keys("refresh_token:*")?.forEach {
            redisTemplate.delete(it)
        }
    }

    @Test
    fun `save - RefreshToken을 Redis에 TTL과 함께 저장한다`() {
        val userId = 1L
        val refreshToken = "test-refresh-token"
        val ttl = Duration.ofHours(1)

        adapter.save(userId, refreshToken, ttl)

        val key = "refresh_token:$userId"
        val stored = redisTemplate.opsForValue().get(key)
        stored shouldBe refreshToken

        val remainingTtl = redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.SECONDS)
        remainingTtl shouldNotBe null
        remainingTtl shouldNotBe -1L // -1은 만료 시간이 설정되지 않음을 의미
    }

    @Test
    fun `findByUserId - 저장된 RefreshToken을 조회한다`() {
        val userId = 1L
        val refreshToken = "test-refresh-token"
        val ttl = Duration.ofHours(1)

        adapter.save(userId, refreshToken, ttl)

        val found = adapter.findByUserId(userId)

        found shouldBe refreshToken
    }

    @Test
    fun `findByUserId - 저장되지 않은 userId면 null을 반환한다`() {
        val userId = 999L

        val found = adapter.findByUserId(userId)

        found shouldBe null
    }

    @Test
    fun `deleteByUserId - RefreshToken을 삭제한다`() {
        val userId = 1L
        val refreshToken = "test-refresh-token"
        val ttl = Duration.ofHours(1)

        adapter.save(userId, refreshToken, ttl)

        adapter.deleteByUserId(userId)

        val found = adapter.findByUserId(userId)
        found shouldBe null
    }

    @Test
    fun `save - 동일한 userId로 재저장하면 기존 토큰을 덮어쓴다`() {
        val userId = 1L
        val oldToken = "old-refresh-token"
        val newToken = "new-refresh-token"
        val ttl = Duration.ofHours(1)

        adapter.save(userId, oldToken, ttl)
        adapter.save(userId, newToken, ttl)

        val found = adapter.findByUserId(userId)
        found shouldBe newToken
    }

    @Test
    fun `save - TTL이 지나면 자동으로 만료된다`() {
        val userId = 1L
        val refreshToken = "test-refresh-token"
        val ttl = Duration.ofSeconds(1)

        adapter.save(userId, refreshToken, ttl)

        // TTL 만료 대기
        Thread.sleep(1500)

        val found = adapter.findByUserId(userId)
        found shouldBe null
    }
}
