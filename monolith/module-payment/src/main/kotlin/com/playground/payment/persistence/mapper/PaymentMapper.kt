package com.playground.payment.persistence.mapper

import com.playground.payment.domain.model.Payment
import com.playground.payment.persistence.entity.PaymentJpaEntity

fun PaymentJpaEntity.toDomain() : Payment {
    return Payment(
        id = this.id,
        userId = this.userId,
        orderId = this.orderId,
        amount = this.amount,
        usedPaymentKey = this.usedPaymentKey,
        status = this.status,
        pgTransactionId = this.pgTransactionId,
        approvalNumber = this.approvalNumber,
        failReason = this.failReason,
        approveAt = this.approveAt,
    )
}