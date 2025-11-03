package com.playground.product.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Version

@Entity
class Product(
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
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
        require(stock - quantity >= 0) { "재고가 부족합니다." }
        stock -= quantity
    }

    override fun toString(): String = "Product(id=$id, name=$name, stock=$stock)"
}