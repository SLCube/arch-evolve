package com.playground.order.persistence.entity

import com.playground.common.jpa.domain.BaseEntity
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import jakarta.persistence.*

@Entity
@Table(name = "orders")
class OrderJpaEntity(
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    var totalPrice: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
) : BaseEntity() {
    companion object {
        fun toJpaEntity(domain: Order): OrderJpaEntity {
            return OrderJpaEntity(
                id = domain.id,
                userId = domain.userId,
                totalPrice = domain.totalPrice,
                status = domain.status
            )
        }
    }
}