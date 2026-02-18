package com.playground.payment.persistence.mapper

import com.playground.payment.domain.model.PaymentMethod
import com.playground.payment.persistence.entity.PaymentMethodJpaEntity

fun PaymentMethodJpaEntity.toDomain(): PaymentMethod =
    PaymentMethod(
        id = this.id,
        userId = this.userId,
        billingKey = this.billingKey,
        cardCompany = this.cardCompany,
        cardNumberMasked = this.cardNumberMasked,
        isDefault = this.isDefault,
    )
