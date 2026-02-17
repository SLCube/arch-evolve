package com.playground.payment.domain.model

import com.playground.payment.domain.enum.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

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
    ) {
        this.status = PaymentStatus.COMPLETED
        this.pgTransactionId = pgTransactionId
        this.approvalNumber = approvalNumber
        this.approveAt = LocalDateTime.now()
    }

    fun fail(reason: String?) {
        this.status = PaymentStatus.FAILED
        this.failReason = reason ?: "PG사로부터 상세 오류 정보가 수신되지 않았습니다."
    }

    fun cancel() {
        check(this.status == PaymentStatus.COMPLETED) { "완료된 결제만 취소할 수 있습니다." }
        this.status = PaymentStatus.CANCELLED
    }
}
