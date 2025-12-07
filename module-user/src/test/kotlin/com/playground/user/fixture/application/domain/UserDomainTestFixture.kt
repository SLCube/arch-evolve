package com.playground.user.fixture.application.domain

import com.playground.user.domain.enum.UserRole
import com.playground.user.domain.model.User
import com.playground.user.domain.model.UserAddress

object UserDomainTestFixture {
    fun mockUser(
        id: Long? = 1L,
        loginId: String = "testUser",
        password: String = "password123",
        nickname: String = "테스트유저",
        role: UserRole = UserRole.USER,
        addresses: MutableList<UserAddress> = mutableListOf()
    ) = User(
        id = id,
        loginId = loginId,
        password = password,
        nickname = nickname,
        role = role,
        addresses = addresses,
    )
}