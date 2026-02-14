package com.playground.support

import com.github.fppt.jedismock.RedisServer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

/**
 * Redis Mock 기반 통합 테스트를 위한 추상 클래스
 *
 * - 테스트 실행 속도 향상 (인메모리 Redis Mock)
 * - 실제 Redis 서버 불필요
 * - 포트 충돌 없음 (자동 할당)
 * - H2 인메모리 DB 사용
 *
 * 사용 방법:
 * ```kotlin
 * @SpringBootTest
 * class ProductConcurrencyTest : IntegrationTestSupport() {
 *     // 테스트 코드
 * }
 * ```
 */
abstract class IntegrationTestSupport {

    companion object {
        private val redisServer: RedisServer by lazy {
            RedisServer.newRedisServer().also { it.start() }
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { "localhost" }
            registry.add("spring.data.redis.port") { redisServer.bindPort }

            registry.add("app.redis.stock.initializer.enabled") { "false" }
        }
    }
}
