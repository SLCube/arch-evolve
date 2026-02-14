package com.playground.delivery.persistence.entity

import com.playground.common.persistence.jpa.BaseEntity
import com.playground.delivery.domain.enum.DeliveryStatus
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "delivery")
class DeliveryJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    val id: Long? = null,
    @Column(name = "order_id", nullable = false)
    val orderId: Long,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Embedded
    val deliveryReceiver: DeliveryReceiverEmbedded,
    @Embedded
    val deliveryAddress: DeliveryAddressEmbedded,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: DeliveryStatus = DeliveryStatus.PENDING,
    @Column(name = "shipped_at")
    var shippedAt: LocalDateTime? = null,
    @Column(name = "deliveredAt")
    var deliveredAt: LocalDateTime? = null,
    @Column(name = "failed_at")
    var failedAt: LocalDateTime? = null,
) : BaseEntity() {
    fun updateStatus(
        status: DeliveryStatus,
        shippedAt: LocalDateTime?,
        deliveredAt: LocalDateTime?,
        failedAt: LocalDateTime?,
    ) {
        this.status = status
        this.shippedAt = shippedAt
        this.deliveredAt = deliveredAt
        this.failedAt = failedAt
    }

    companion object {
        fun toJpaEntity(domain: com.playground.delivery.domain.model.Delivery): DeliveryJpaEntity =
            DeliveryJpaEntity(
                id = domain.id,
                orderId = domain.orderId,
                userId = domain.userId,
                deliveryReceiver = DeliveryReceiverEmbedded.from(domain.deliveryReceiver),
                deliveryAddress = DeliveryAddressEmbedded.from(domain.deliveryAddress),
                status = domain.deliveryStatus,
                shippedAt = domain.shippedAt,
                deliveredAt = domain.deliveredAt,
                failedAt = domain.failedAt,
            )
    }
}
