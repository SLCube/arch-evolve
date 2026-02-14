package com.playground.order.domain.model

import com.playground.user.contract.domain.vo.ReceiverAddressInfo

data class OrderAddress(
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
) {
    companion object {
        fun fromAddressInfo(receiverAddressInfo: ReceiverAddressInfo): OrderAddress {
            return OrderAddress(
                zipCode = receiverAddressInfo.zipCode,
                baseAddress = receiverAddressInfo.baseAddress,
                detailAddress = receiverAddressInfo.detailAddress,
            )
        }
    }
}
