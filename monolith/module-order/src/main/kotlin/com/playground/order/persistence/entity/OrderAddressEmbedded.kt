package com.playground.order.persistence.entity

import com.playground.order.domain.model.OrderAddress
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class OrderAddressEmbedded(
    @Column(name = "delivery_zip_code", nullable = false, length = 20)
    val zipCode: String,

    @Column(name = "delivery_base_address", nullable = false, length = 255)
    val baseAddress: String,

    @Column(name = "delivery_detail_address", nullable = false, length = 255)
    val detailAddress: String,
) {
    companion object {
        fun toEmbedded(domain: OrderAddress): OrderAddressEmbedded {
            return OrderAddressEmbedded(
                zipCode = domain.zipCode,
                baseAddress = domain.baseAddress,
                detailAddress = domain.detailAddress,
            )
        }
    }
}