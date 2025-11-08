package com.playground.common.security.annotation

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@authChecker.isOwner(principal.username, #userId)")
annotation class CheckIsOwner()
