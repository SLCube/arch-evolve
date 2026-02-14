package com.playground.user.application.service

import com.playground.user.application.port.outbound.UserCommandPort
import com.playground.user.application.port.outbound.UserQueryPort
import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.fixture.application.command.UserAddressCommandTestFixture
import com.playground.user.fixture.application.domain.UserDomainTestFixture
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.Optional

@Suppress("NonAsciiCharacters")
class UserAddressServiceTest {

    private val userQueryPort: UserQueryPort = mock()
    private val userCommandPort: UserCommandPort = mock()

    private val userAddressService = UserAddressService(
        userQueryPort = userQueryPort,
        userCommandPort = userCommandPort,
    )

    @Test
    fun `주소 등록 시 사용자 애그리게이트에 주소를 추가하고 저장한다`() {
        // given
        val userId = 10L
        val command = UserAddressCommandTestFixture.registerCommand(userId = userId)
        val aggregate = UserDomainTestFixture.mockUser(
            id = userId,
            addresses = mutableListOf(),
        )

        given(userQueryPort.findUserWithAddressById(userId))
            .willReturn(Optional.of(aggregate))
        given(userCommandPort.update(any()))
            .willReturn(aggregate)

        // when
        userAddressService.registerAddress(command)

        // then
        verify(userCommandPort).update(check { saved ->
            saved.addresses.size shouldBe 1
            val newAddress = saved.addresses.first()
            newAddress.receiverName shouldBe command.receiverName
            newAddress.receiverPhoneNumber shouldBe command.receiverPhoneNumber
            newAddress.zipCode shouldBe command.zipCode
            newAddress.baseAddress shouldBe command.baseAddress
            newAddress.detailAddress shouldBe command.detailAddress
            newAddress.isDefault shouldBe true
        })
    }

    @Test
    fun `기본 주소 설정 시 선택한 주소만 기본으로 변경한다`() {
        // given
        val userId = 5L
        val primaryAddress = UserDomainTestFixture.mockUserAddress(
            id = 1L,
            userId = userId,
            receiverName = "기존",
            receiverPhoneNumber = "01000000000",
            zipCode = "11111",
            baseAddress = "서울시 용산구",
            detailAddress = "101동",
            isDefault = true,
        )
        val targetAddress = UserDomainTestFixture.mockUserAddress(
            id = 2L,
            userId = userId,
            receiverName = "새주소",
            receiverPhoneNumber = "01099999999",
            zipCode = "22222",
            baseAddress = "부산시 해운대구",
            detailAddress = "202동",
            isDefault = false,
        )
        val aggregate = UserDomainTestFixture.mockUser(
            id = userId,
            addresses = mutableListOf(primaryAddress, targetAddress),
        )
        val command = UserAddressCommandTestFixture.defaultSetCommand(
            userId = userId,
            addressId = targetAddress.id!!,
        )

        given(userQueryPort.findUserWithAddressById(userId))
            .willReturn(Optional.of(aggregate))

        // when
        userAddressService.setDefaultAddress(command)

        // then
        verify(userCommandPort).update(check { saved ->
            saved.addresses.find { it.id == primaryAddress.id }!!.isDefault shouldBe false
            saved.addresses.find { it.id == targetAddress.id }!!.isDefault shouldBe true
        })
    }

    @Test
    fun `주소 등록 시 사용자를 찾을 수 없으면 예외가 발생한다`() {
        // given
        val command = UserAddressCommandTestFixture.registerCommand(userId = 99L)
        given(userQueryPort.findUserWithAddressById(command.userId))
            .willReturn(Optional.empty())

        // when & then
        assertThrows<UserNotFoundException> {
            userAddressService.registerAddress(command)
        }
        verify(userCommandPort, org.mockito.kotlin.never()).update(any())
    }
}
