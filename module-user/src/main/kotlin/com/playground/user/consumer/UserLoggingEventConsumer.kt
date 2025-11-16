package com.playground.user.consumer

import com.playground.common.log.utils.logger
import com.playground.user.domain.event.UserNicknameUpdatedEvent
import com.playground.user.domain.event.UserPasswordUpdatedEvent
import com.playground.user.domain.event.UserSignedUpEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserLoggingEventConsumer {
    private val log = logger()

    @TransactionalEventListener
    fun handleUserSignedUpEvent(event: UserSignedUpEvent) {
        log.info("New user signed up. userId={}, loginId={}", event.userId, event.loginId)
    }

    @TransactionalEventListener
    fun handleUserNicknameUpdatedEvent(event: UserNicknameUpdatedEvent) {
        log.info(
            "User nickname updated. userId={}, loginId={}, oldNickname={}, newNickname={}",
            event.userId,
            event.loginId,
            event.oldNickname,
            event.newNickname,
        )
    }

    @TransactionalEventListener
    fun handleUserPasswordUpdatedEvent(event: UserPasswordUpdatedEvent) {
        log.info("User password updated. userId={}, loginId={}", event.userId, event.loginId)
    }
}
