package com.playground.product.domain.model

import java.math.BigDecimal

class Product(
    val id: Long? = null,
    var name: String,
    var stock: Int,
    var price: BigDecimal,
) {
    fun update(
        name: String,
        stock: Int,
        price: BigDecimal,
    ) {
        this.name = name
        this.stock = stock
        this.price = price
    }

    override fun toString(): String = "Product(id=$id, name=$name, stock=$stock, price=$price)"
}
