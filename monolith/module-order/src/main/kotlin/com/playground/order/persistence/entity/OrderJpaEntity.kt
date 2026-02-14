package com.playground.order.persistence.entity

import com.playground.common.persistence.jpa.BaseEntity
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "orders")
class OrderJpaEntity(
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false, precision = 19, scale = 2)
    var totalPrice: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
    @Embedded
    val orderAddress: OrderAddressEmbedded,
    @Embedded
    val orderReceiver: OrderReceiverEmbedded,
    @Column(nullable = true, length = 50)
    var pgTransactionId: String? = null,
) : BaseEntity() {
    companion object {
        fun toJpaEntity(domain: Order): OrderJpaEntity =
            OrderJpaEntity(
                id = domain.id,
                userId = domain.userId,
                totalPrice = domain.totalPrice,
                status = domain.status,
                orderAddress = OrderAddressEmbedded.toEmbedded(domain.orderAddress),
                orderReceiver = OrderReceiverEmbedded.toEmbedded(domain.orderReceiver),
            )
    }

    fun updateFromDomain(order: Order) {
        this.totalPrice = order.totalPrice
        this.status = order.status
        this.pgTransactionId = order.pgTransactionId
    }
}
