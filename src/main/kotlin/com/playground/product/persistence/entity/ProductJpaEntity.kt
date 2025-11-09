package com.playground.product.persistence.entity

import com.playground.common.jpa.domain.BaseEntity
import com.playground.product.domain.Product
import jakarta.persistence.*

@Entity
@Table(name = "products")
class ProductJpaEntity(
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var name: String,
    var stock: Int,
    var price: Long
): BaseEntity() {
    companion object {
        fun toJpaEntity(domain: Product): ProductJpaEntity {
            return ProductJpaEntity(
                id = domain.id,
                name = domain.name,
                stock = domain.stock,
                price = domain.price
            )
        }
    }

    fun updateFromDomain(domain: Product) {
        this.name = domain.name
        this.stock = domain.stock
        this.price = domain.price
    }
}