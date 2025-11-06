package com.playground.product.domain

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

    @Version
    val version: Long = 0L
) {
    fun update(name: String, stock: Int) {
        this.name = name
        this.stock = stock
    }

    fun decreaseStock(quantity: Int) {
        if(stock - quantity < 0) {
            throw InsufficientStockException()
        }

        stock -= quantity
    }

    override fun toString(): String = "Product(id=$id, name=$name, stock=$stock)"
}