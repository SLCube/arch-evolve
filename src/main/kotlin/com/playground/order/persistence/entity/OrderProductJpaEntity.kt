package com.playground.order.persistence.entity

import com.playground.common.jpa.domain.BaseEntity
import com.playground.order.domain.model.OrderProduct
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "order_products")
class OrderProductJpaEntity(
    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var orderJpaEntity: OrderJpaEntity,

    @Column(nullable = false)
    val productId: Long,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false)
    val price: Long
): BaseEntity() {
    companion object {
        fun toJpaEntity(domain: OrderProduct, orderJpaEntity: OrderJpaEntity): OrderProductJpaEntity {
            return OrderProductJpaEntity(
                id = domain.id,
                orderJpaEntity = orderJpaEntity,
                productId = domain.productId,
                quantity = domain.quantity,
                price = domain.price
            )
        }
    }
}