package com.playground.product.persistence.entity

import com.playground.common.persistence.jpa.BaseEntity
import com.playground.product.domain.model.Product
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "products")
class ProductJpaEntity(
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var name: String,
    var stock: Int,
    @Column(nullable = false, precision = 19, scale = 2)
    var price: BigDecimal,
) : BaseEntity() {
    companion object {
        fun toJpaEntity(domain: Product): ProductJpaEntity =
            ProductJpaEntity(
                id = domain.id,
                name = domain.name,
                stock = domain.stock,
                price = domain.price,
            )
    }

    fun updateFromDomain(domain: Product) {
        this.name = domain.name
        this.stock = domain.stock
        this.price = domain.price
    }
}
