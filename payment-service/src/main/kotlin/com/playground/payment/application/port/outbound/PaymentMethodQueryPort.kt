package com.playground.payment.application.port.outbound

import com.playground.payment.domain.model.PaymentMethod
import java.util.Optional

interface PaymentMethodQueryPort {
    fun getDefaultByUserId(userId: Long): PaymentMethod

    /**
     * 사용자의 기본 결제 수단을 조회하며, 없을 경우 null을 반환합니다.
     * (예외 처리가 필요 없는 존재 유무 체크용)
     */
    fun findDefaultOrNullByUserId(userId: Long): Optional<PaymentMethod>

    fun findAllByUserId(userId: Long): List<PaymentMethod>

    fun getById(paymentMethodId: Long): PaymentMethod

    fun countByUserId(userId: Long): Long
}
