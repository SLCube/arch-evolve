package com.playground.user.fixture.presentation.request

import com.playground.user.presentation.request.AddressRegisterRequestDto

object AddressRequestTestFixture {
    fun mockAddressRegisterRequest(
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "01012345678",
        zipCode: String = "12345",
        baseAddress: String = "서울특별시 중구 세종대로",
        detailAddress: String = "101동 1001호",
    ) = AddressRegisterRequestDto(
        receiverName = receiverName,
        receiverPhoneNumber = receiverPhoneNumber,
        zipCode = zipCode,
        baseAddress = baseAddress,
        detailAddress = detailAddress,
    )
}
