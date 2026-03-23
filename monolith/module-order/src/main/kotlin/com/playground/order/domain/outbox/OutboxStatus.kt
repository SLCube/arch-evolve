package com.playground.order.domain.outbox

enum class OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED,
}
