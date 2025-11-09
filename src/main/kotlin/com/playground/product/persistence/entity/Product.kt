package com.playground.product.persistence.entity

import com.playground.common.jpa.domain.BaseEntity
import com.playground.product.domain.exception.InsufficientStockException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Product(
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var name: String,
    var stock: Int,
    var price: Long
): BaseEntity() {
    fun update(name: String, stock: Int, price: Long) {
        this.name = name
        this.stock = stock
        this.price = price
    }

    fun decreaseStock(quantity: Int) {
        val currentId = requireNotNull(id) { "재고 차감을 위한 Product의 Id가 존재하지 않습니다." }
        if(stock - quantity < 0) {
            throw InsufficientStockException(currentId, stock, quantity)
        }

        stock -= quantity
    }

    override fun toString(): String = "Product(id=$id, name=$name, stock=$stock, price=$price)"
}