package com.playground.order.fixture

import com.playground.user.contract.domain.vo.ReceiverAddressInfo

object AddressInfoTestFixture {
    fun mockAddressInfo(
        zipCode: String = "12345",
        baseAddress: String = "서울특별시",
        detailAddress: String = "강남구 역삼동",
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "010-1234-5678",
    )= ReceiverAddressInfo(
        zipCode = zipCode,
        baseAddress = baseAddress,
        detailAddress = detailAddress,
        receiverName = receiverName,
        receiverPhoneNumber = receiverPhoneNumber,
    )
}