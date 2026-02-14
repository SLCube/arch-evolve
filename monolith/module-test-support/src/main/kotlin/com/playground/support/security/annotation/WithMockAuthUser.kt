package com.playground.support.security.annotation

import com.playground.support.security.factory.AuthUserSecurityContextFactory
import org.springframework.security.test.context.support.WithSecurityContext

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = AuthUserSecurityContextFactory::class)
annotation class WithMockAuthUser(
    val userId: Long = 2L,
    val loginId: String = "testuser",
    val password: String = "password",
    val role: String = "USER",
)
