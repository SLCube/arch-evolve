package com.playground.product.persistence.repository

import com.playground.product.persistence.entity.ProductJpaEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ProductRepository : JpaRepository<ProductJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductJpaEntity p where p.id = :id")
    fun findByIdWithPessimisticLock(id: Long): Optional<ProductJpaEntity>
}