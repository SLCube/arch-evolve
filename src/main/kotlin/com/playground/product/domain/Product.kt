package com.playground.product.domain

import com.playground.product.domain.exception.InsufficientStockException

class Product(
    val id: Long? = null,
    var name: String,
    var stock: Int,
    var price: Long,
) {
    fun update(
        name: String,
        stock: Int,
        price: Long,
    ) {
        this.name = name
        this.stock = stock
        this.price = price
    }

    fun decreaseStock(quantity: Int) {
        val currentId = id!!
        if (stock - quantity < 0) {
            throw InsufficientStockException(currentId, stock, quantity) // orderId 파라미터 제거
        }

        stock -= quantity
    }

    override fun toString(): String = "Product(id=$id, name=$name, stock=$stock, price=$price)"
}
