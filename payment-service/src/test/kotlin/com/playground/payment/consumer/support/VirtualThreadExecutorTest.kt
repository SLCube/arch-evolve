package com.playground.payment.consumer.support

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.Callable

@Suppress("NonAsciiCharacters")
class VirtualThreadExecutorTest {
    private val executor = VirtualThreadExecutor()

    @Test
    fun `모든 callable이 성공하면 결과 리스트를 반환해야 한다`() {
        val callables =
            listOf<Callable<Int>>(
                Callable { 1 },
                Callable { 2 },
                Callable { 3 },
            )

        val result = executor.invokeAll(callables)

        result shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `callable 중 하나가 실패하면 원본 예외를 전파해야 한다`() {
        val callables =
            listOf<Callable<Int>>(
                Callable { 1 },
                Callable { throw RuntimeException("처리 실패") },
                Callable { 3 },
            )

        val thrown =
            assertThrows<RuntimeException> {
                executor.invokeAll(callables)
            }

        thrown.message shouldBe "처리 실패"
    }

    @Test
    fun `ExecutionException은 언래핑하여 원본 예외를 전파해야 한다`() {
        val cause = IllegalStateException("원본 예외")
        val callables =
            listOf<Callable<Unit>>(
                Callable { throw cause },
            )

        val thrown =
            assertThrows<IllegalStateException> {
                executor.invokeAll(callables)
            }

        thrown.message shouldBe "원본 예외"
    }
}
