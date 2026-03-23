package com.playground.delivery.infra.metrics

import com.playground.delivery.application.port.outbound.OutboxMetricsPort
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class OutboxMetricsAdapter(
    meterRegistry: MeterRegistry,
) : OutboxMetricsPort {
    private val pendingCount = AtomicLong(0)
    private val failedCount = AtomicLong(0)

    init {
        meterRegistry.gauge(
            "outbox.pending.count",
            listOf(Tag.of("service", "delivery")),
            pendingCount,
        )
        meterRegistry.gauge(
            "outbox.failed.count",
            listOf(Tag.of("service", "delivery")),
            failedCount,
        )
    }

    override fun recordPendingCount(count: Long) {
        pendingCount.set(count)
    }

    override fun recordFailedCount(count: Long) {
        failedCount.set(count)
    }
}
