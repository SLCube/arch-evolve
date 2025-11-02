package com.playground.product.domain

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
    val id: Long = 0L,
    var name: String,
    var stock: Int,
) {
    fun update(name: String, stock: Int) {
        this.name = name
        this.stock = stock
    }

    override fun toString(): String = "Product(id=$id, name=$name, stock=$stock)"
}