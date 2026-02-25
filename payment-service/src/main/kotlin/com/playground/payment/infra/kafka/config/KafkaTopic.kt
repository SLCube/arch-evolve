package com.playground.payment.infra.kafka.config

import com.playground.payment.domain.outbox.OutboxEventType

object KafkaTopic {
    const val PAYMENT_AUTHORIZED = "payment.authorized"
    const val PAYMENT_FAILED = "payment.failed"

    fun from(eventType: OutboxEventType): String =
        when (eventType) {
            OutboxEventType.PAYMENT_AUTHORIZED -> PAYMENT_AUTHORIZED
            OutboxEventType.PAYMENT_FAILED -> PAYMENT_FAILED
        }
}
