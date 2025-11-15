package com.playground.user.domain.event

import com.playground.common.event.DomainEvent

data class UserSignedUpEvent(
    val userId: Long,
    val loginId: String,
) : DomainEvent
