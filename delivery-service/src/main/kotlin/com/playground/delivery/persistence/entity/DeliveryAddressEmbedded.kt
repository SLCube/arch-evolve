package com.playground.delivery.persistence.entity

import com.playground.delivery.domain.model.DeliveryAddress
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class DeliveryAddressEmbedded(
    @Column(name = "zip_code", nullable = false, length = 10)
    val zipCode: String,
    @Column(name = "base_address", nullable = false, length = 255)
    val baseAddress: String,
    @Column(name = "detail_address", nullable = false, length = 255)
    val detailAddress: String,
) {
    companion object {
        fun from(address: DeliveryAddress): DeliveryAddressEmbedded =
            DeliveryAddressEmbedded(
                zipCode = address.zipCode,
                baseAddress = address.baseAddress,
                detailAddress = address.detailAddress,
            )
    }
}
