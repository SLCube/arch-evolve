package com.playground.payment.fixture.application.domain

import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.model.Payment
import java.math.BigDecimal
import java.time.LocalDateTime

object PaymentDomainTestFixture {
    fun mockPayment(
        id: Long? = 1L,
        userId: Long = 2L,
        orderId: Long = 100L,
        amount: BigDecimal = BigDecimal("10000.00"),
        usedPaymentKey: String = "billing-key",
        status: PaymentStatus = PaymentStatus.PENDING,
        pgTransactionId: String? = null,
        approvalNumber: String? = null,
        failReason: String? = null,
        createdAt: LocalDateTime = LocalDateTime.of(2024, 1, 1, 0, 0),
        approveAt: LocalDateTime? = null,
    ) = Payment(
        id = id,
        userId = userId,
        orderId = orderId,
        amount = amount,
        usedPaymentKey = usedPaymentKey,
        status = status,
        pgTransactionId = pgTransactionId,
        approvalNumber = approvalNumber,
        failReason = failReason,
        createdAt = createdAt,
        approveAt = approveAt,
    )
}
