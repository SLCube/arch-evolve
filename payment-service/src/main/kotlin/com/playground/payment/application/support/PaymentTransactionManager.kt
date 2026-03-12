package com.playground.payment.application.support

import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.application.port.outbound.OutboxCommandPort
import com.playground.payment.application.port.outbound.PaymentCommandPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.application.port.outbound.PaymentQueryPort
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.model.Payment
import com.playground.payment.domain.model.PaymentMethod
import com.playground.payment.domain.vo.PgAuthorizationResult
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 결제 처리 흐름에서 트랜잭션 경계를 관리하는 컴포넌트.
 *
 * [PaymentService]는 PG(외부 결제) 호출을 트랜잭션 밖에서 실행해야 한다.
 * 만약 서비스 메서드 전체에 @Transactional을 걸면 외부 HTTP 호출 동안 DB Connection이
 * 점유되는 문제가 발생하므로, 이 클래스를 통해 트랜잭션을 메서드 단위로 잘게 분리한다.
 *
 * - 조회 메서드: 각각 독립적인 readOnly 트랜잭션으로 실행
 * - [savePaymentResult]: 결제 저장 + Outbox 저장을 하나의 트랜잭션으로 보장 (Outbox Pattern)
 *
 * @see PaymentService 실제 결제 흐름 오케스트레이션
 */
@Component
class PaymentTransactionManager(
    private val paymentQueryPort: PaymentQueryPort,
    private val paymentCommandPort: PaymentCommandPort,
    private val paymentMethodQueryPort: PaymentMethodQueryPort,
    private val outboxCommandPort: OutboxCommandPort,
    private val outboxFactory: OutboxFactory,
) {
    fun findByOrderId(orderId: Long): Payment? = paymentQueryPort.findByOrderId(orderId)

    fun getPaymentMethod(userId: Long): PaymentMethod = paymentMethodQueryPort.findDefaultByUserId(userId)

    @Transactional
    fun savePaymentResult(
        command: PaymentAuthorizeCommand,
        pgResult: PgAuthorizationResult,
        billingKey: String,
    ): Payment {
        val payment =
            Payment(
                userId = command.userId,
                orderId = command.orderId,
                amount = command.amount,
                usedPaymentKey = billingKey,
                status = PaymentStatus.PENDING,
            )

        val domainEvent =
            if (pgResult.isSuccess) {
                payment.complete(pgResult.requirePgTransactionId(), pgResult.requireApprovalNumber())
            } else {
                payment.fail(pgResult.failReason)
            }
        val savedPayment = paymentCommandPort.save(payment)
        outboxCommandPort.save(outboxFactory.from(domainEvent))
        return savedPayment
    }
}
