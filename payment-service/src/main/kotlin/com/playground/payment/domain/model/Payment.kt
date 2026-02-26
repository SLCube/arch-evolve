package com.playground.payment.domain.model

import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.event.PaymentAuthorizedEvent
import com.playground.payment.domain.event.PaymentDomainEvent
import com.playground.payment.domain.event.PaymentFailedEvent
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class Payment(
    val id: Long? = null,
    val userId: Long,
    val orderId: Long,
    val amount: BigDecimal,
    val usedPaymentKey: String,
    var status: PaymentStatus,
    var pgTransactionId: String? = null,
    var approvalNumber: String? = null,
    var failReason: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var approveAt: LocalDateTime? = null,
) {
    fun complete(
        pgTransactionId: String,
        approvalNumber: String,
    ): PaymentDomainEvent {
        val now = LocalDateTime.now()
        this.status = PaymentStatus.COMPLETED
        this.pgTransactionId = pgTransactionId
        this.approvalNumber = approvalNumber
        this.approveAt = now
        return PaymentAuthorizedEvent(
            eventId = UUID.randomUUID(),
            orderId = this.orderId,
            userId = this.userId,
            amount = this.amount,
            pgTransactionId = pgTransactionId,
            occurredAt = now,
        )
    }

    fun fail(reason: String?): PaymentDomainEvent {
        val failReason = reason ?: "PG사로부터 상세 오류 정보가 수신되지 않았습니다."
        this.status = PaymentStatus.FAILED
        this.failReason = failReason
        return PaymentFailedEvent(
            eventId = UUID.randomUUID(),
            orderId = this.orderId,
            userId = this.userId,
            failReason = failReason,
            occurredAt = LocalDateTime.now(),
        )
    }

    fun cancel() {
        check(this.status == PaymentStatus.COMPLETED) { "완료된 결제만 취소할 수 있습니다." }
        this.status = PaymentStatus.CANCELLED
    }
}
