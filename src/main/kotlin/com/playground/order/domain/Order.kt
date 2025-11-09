package com.playground.order.domain

import com.playground.common.jpa.domain.BaseEntity
import com.playground.order.enum.OrderStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "orders")
class Order(
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

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val orderItems: MutableList<OrderItem> = mutableListOf(),

): BaseEntity() {

    fun addOrderItem(orderItem: OrderItem) {
        orderItems.add(orderItem)
        orderItem.order = this
    }

    fun calculateTotalPrice() {
        this.totalPrice = orderItems.sumOf { it.price * it.quantity }
    }

    fun completeOrder() {
        require(this.status == OrderStatus.PENDING) { "주문 상태가 PENDING일 때만 완료할 수 있습니다." }
        this.status = OrderStatus.COMPLETED
    }

    fun cancelOrder() {
        require(this.status == OrderStatus.PENDING || this.status == OrderStatus.COMPLETED) { "주문 상태가 PENDING 또는 COMPLETED일 때만 취소할 수 있습니다." }
        this.status = OrderStatus.CANCELLED
    }
}