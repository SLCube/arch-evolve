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

    fun mockUserAddress(
        id: Long? = 1L,
        userId: Long = 1L,
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "01011112222",
        zipCode: String = "12345",
        baseAddress: String = "서울특별시 중구 세종대로",
        detailAddress: String = "101동 1001호",
        isDefault: Boolean = false,
    ) = UserAddress(
        id = id,
        userId = userId,
        receiverName = receiverName,
        receiverPhoneNumber = receiverPhoneNumber,
        zipCode = zipCode,
        baseAddress = baseAddress,
        detailAddress = detailAddress,
        isDefault = isDefault,
    )
}
