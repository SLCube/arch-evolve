package com.playground.order.consumer.config

object KafkaConsumerTopic {
    const val PAYMENT_AUTHORIZED = "payment.authorized"
    const val PAYMENT_FAILED = "payment.failed"
    const val PAYMENT_AUTHORIZED_DLT = "payment-authorized.DLT"
    const val PAYMENT_FAILED_DLT = "payment-failed.DLT"
}
