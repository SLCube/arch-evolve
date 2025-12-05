package com.playground.user.persistence.adapter

import com.playground.user.contract.application.port.outbound.AddressInfoQueryPort
import com.playground.user.contract.domain.vo.ReceiverAddressInfo
import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class AddressInfoQueryAdapter(
    private val userRepository: UserRepository,
): AddressInfoQueryPort {
    override fun getAddressInfoByAddressId(
        userId: Long,
        addressId: Long
    ): ReceiverAddressInfo {
        TODO("Not yet implemented")
    }
}