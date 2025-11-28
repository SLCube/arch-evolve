package com.playground.common.jpa.config

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuerydslConfig(
    @field:PersistenceContext
    private val entityManager: EntityManager,
) {

    @Bean
    fun jpaQueryFactory() : JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}