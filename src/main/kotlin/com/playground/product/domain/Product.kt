package com.playground.product.domain

import com.playground.common.jpa.domain.BaseEntity
import com.playground.product.exception.InsufficientStockException
import jakarta.persistence.*

@Entity
class Product(
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var name: String,
    var stock: Int,
): BaseEntity() {
    fun update(name: String, stock: Int) {
        this.name = name
        this.stock = stock
    }

    fun decreaseStock(quantity: Int) {
        val currentId = requireNotNull(id) { "재고 차감을 위한 Product의 Id가 존재하지 않습니다." }
        if(stock - quantity < 0) {
            throw InsufficientStockException(currentId, stock, quantity)
        }

        stock -= quantity
    }

    override fun toString(): String = "Product(id=$id, name=$name, stock=$stock)"
}