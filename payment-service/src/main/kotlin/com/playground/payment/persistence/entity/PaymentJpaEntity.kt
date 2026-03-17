package com.playground.payment.persistence.entity

import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.model.Payment
import com.playground.payment.persistence.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments", indexes = [Index(name = "idx_payments_order_id", columnList = "order_id")])
class PaymentJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    val id: Long? = null,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val orderId: Long,
    @Column(nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal,
    @Column(nullable = false)
    val usedPaymentKey: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus,
    var pgTransactionId: String? = null,
    var approvalNumber: String? = null,
    var failReason: String? = null,
    var approveAt: LocalDateTime? = null,
) : BaseEntity() {
    companion object {
        fun toJpaEntity(domain: Payment): PaymentJpaEntity =
            PaymentJpaEntity(
                id = domain.id,
                userId = domain.userId,
                orderId = domain.orderId,
                amount = domain.amount,
                usedPaymentKey = domain.usedPaymentKey,
                status = domain.status,
                pgTransactionId = domain.pgTransactionId,
                approvalNumber = domain.approvalNumber,
                failReason = domain.failReason,
                approveAt = domain.approveAt,
            )
    }

    fun updateFromDomain(domain: Payment) {
        this.status = domain.status
        this.pgTransactionId = domain.pgTransactionId
        this.approvalNumber = domain.approvalNumber
        this.failReason = domain.failReason
        this.approveAt = domain.approveAt
    }
}
