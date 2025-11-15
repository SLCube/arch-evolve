package com.playground.user.domain.event

import com.playground.common.event.DomainEvent

data class UserNicknameUpdatedEvent(
    val userId: Long,
    val loginId: String,
    val oldNickname: String,
    val newNickname: String
) : DomainEvent