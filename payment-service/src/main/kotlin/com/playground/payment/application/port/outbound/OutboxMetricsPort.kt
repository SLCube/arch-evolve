package com.playground.payment.application.port.outbound

interface OutboxMetricsPort {
    fun recordPendingCount(count: Long)

    fun recordFailedCount(count: Long)
}
