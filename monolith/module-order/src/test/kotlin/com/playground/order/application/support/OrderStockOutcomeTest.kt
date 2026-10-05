package com.playground.order.application.support

import com.playground.order.contract.port.OrderStockOutcome
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.persistence.adapter.OrderStockRecoveryAdapter
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.repository.OrderRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.transaction.AfterTransaction
import org.springframework.test.context.transaction.TestTransaction

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(OrderStockRecoveryAdapter::class)
class OrderStockOutcomeTest(
    @param:Autowired private val orders: OrderRepository,
    @param:Autowired private val recovery: OrderStockRecoveryAdapter,
) {
    @AfterTransaction
    fun cleanUp() {
        orders.deleteAll()
    }

    @Test
    fun `DB에 없는 주문은 예약 해제 대상으로 판단하지 않는다`() {
        TestTransaction.end()
        recovery.inspect(123L) shouldBe OrderStockOutcome.UNKNOWN
    }

    @Test
    fun `아직 커밋되지 않은 주문은 결과를 알 수 없는 상태로 반환한다`() {
        val order = save(OrderStatus.PENDING)
        recovery.inspect(order.id!!) shouldBe OrderStockOutcome.UNKNOWN
    }

    @Test
    fun `커밋된 주문 상태에 따라 복구 대상을 구분한다`() {
        val cases = mapOf(
            OrderStatus.PENDING to OrderStockOutcome.PENDING,
            OrderStatus.COMPLETED to OrderStockOutcome.CONFIRMED,
            OrderStatus.FAILED to OrderStockOutcome.RELEASED,
            OrderStatus.CANCELLED to OrderStockOutcome.RELEASED,
        )
        val saved = cases.map { (status, expected) -> save(status).id!! to expected }
        TestTransaction.flagForCommit()
        TestTransaction.end()
        saved.forEach { (id, expected) -> recovery.inspect(id) shouldBe expected }
    }

    private fun save(status: OrderStatus): OrderJpaEntity =
        orders.saveAndFlush(
            OrderJpaEntity.toJpaEntity(OrderDomainTestFixture.mockOrder(id = null, status = status)),
        )
}
