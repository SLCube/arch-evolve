package com.playground.product.persistence.repository

import com.playground.product.persistence.entity.ProductJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : ProductCustomRepository, JpaRepository<ProductJpaEntity, Long>
