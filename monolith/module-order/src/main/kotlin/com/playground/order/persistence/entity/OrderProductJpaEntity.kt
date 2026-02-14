package com.playground.order.persistence.entity

import com.playground.common.persistence.jpa.BaseEntity
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
import java.math.BigDecimal

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
    @Column(nullable = false, precision = 19, scale = 2)
    val price: BigDecimal,
) : BaseEntity() {
    companion object {
        fun toJpaEntity(
            domain: OrderProduct,
            orderJpaEntity: OrderJpaEntity,
        ): OrderProductJpaEntity =
            OrderProductJpaEntity(
                id = domain.id,
                orderJpaEntity = orderJpaEntity,
                productId = domain.productId,
                quantity = domain.quantity,
                price = domain.price,
            )
    }
}
