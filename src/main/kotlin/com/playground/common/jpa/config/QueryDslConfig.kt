package com.playground.common.jpa.config

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Configuration

@Configuration
class QueryDslConfig {

    @PersistenceContext
    private lateinit var entityManager: EntityManager
}