package com.playground.payment.domain.outbox

enum class OutboxEventType {
    PAYMENT_AUTHORIZED,
    PAYMENT_FAILED,
}
