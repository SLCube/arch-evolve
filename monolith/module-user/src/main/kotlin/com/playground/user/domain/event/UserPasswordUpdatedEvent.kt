package com.playground.user.domain.event

import com.playground.common.event.DomainEvent

data class UserPasswordUpdatedEvent(
    val userId: Long,
    val loginId: String,
) : DomainEvent
