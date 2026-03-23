package com.playground.delivery.domain.outbox

enum class OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED,
}
