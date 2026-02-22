package com.playground.order.application.port.outbound

import java.math.BigDecimal

interface PaymentPort {
    fun authorize(userId: Long, orderId: Long, amount: BigDecimal): String // pgTransactionId
}
