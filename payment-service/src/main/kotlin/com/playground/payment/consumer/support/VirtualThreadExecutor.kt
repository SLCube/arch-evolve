package com.playground.payment.consumer.support

import com.playground.payment.common.log.utils.logger
import org.springframework.stereotype.Component
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

@Component
class VirtualThreadExecutor {
    private val log = logger()

    fun <T> invokeAll(callables: List<Callable<T>>): List<T> =
        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val results =
                executor.invokeAll(callables).map { future ->
                    runCatching { future.get() }.recoverCatching { e ->
                        throw if (e is ExecutionException) e.cause ?: e else e
                    }
                }
            val errors = results.mapNotNull { it.exceptionOrNull() }
            errors.forEach { log.error("VT 처리 실패", it) }
            if (errors.isNotEmpty()) throw errors.first()
            results.map { it.getOrThrow() }
        }
}
