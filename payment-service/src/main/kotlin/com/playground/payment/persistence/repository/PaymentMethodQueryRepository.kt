package com.playground.payment.persistence.repository

import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import java.util.Optional

fun interface PaymentMethodQueryRepository {
    fun findDefaultByUserId(userId: Long): Optional<PaymentMethodJpaEntity>
}
