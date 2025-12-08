package com.playground.payment.fixture.application.domain

import com.playground.payment.domain.model.PaymentMethod
import java.time.LocalDateTime

object PaymentMethodDomainTestFixture {
    fun mockPaymentMethod(
        id: Long? = 1L,
        userId: Long = 2L,
        billingKey: String = "billing-key",
        cardCompany: String = "테스트카드",
        cardNumberMasked: String = "****-1234",
        isDefault: Boolean = true,
        createdAt: LocalDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0),
        updatedAt: LocalDateTime = createdAt,
    ) = PaymentMethod(
        id = id,
        userId = userId,
        billingKey = billingKey,
        cardCompany = cardCompany,
        cardNumberMasked = cardNumberMasked,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
