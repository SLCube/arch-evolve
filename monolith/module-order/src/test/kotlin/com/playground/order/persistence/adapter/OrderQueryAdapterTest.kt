package com.playground.order.persistence.adapter

import com.playground.common.application.query.PageQuery
import com.playground.order.domain.exception.OrderNotFoundException
import com.playground.order.domain.model.OrderProduct
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.entity.OrderProductJpaEntity
import com.playground.order.persistence.repository.OrderProductRepository
import com.playground.order.persistence.repository.OrderRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.math.BigDecimal

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(OrderQueryAdapter::class)
class OrderQueryAdapterTest(
    @param:Autowired private val orderQueryAdapter: OrderQueryAdapter,
    @param:Autowired private val orderRepository: OrderRepository,
    @param:Autowired private val orderProductRepository: OrderProductRepository,
) {

    private val initialUserId = 10L

    private fun saveTestOrder(userId: Long, orderId: Long? = null, totalProductCount: Int = 2): OrderJpaEntity {
        val orderDomain = OrderDomainTestFixture.mockOrder(id = orderId, userId = userId)
        val orderJpaEntity = OrderJpaEntity.toJpaEntity(orderDomain)
        val savedOrderEntity = orderRepository.save(orderJpaEntity)

        val baseProductDomain = OrderDomainTestFixture.mockUnsavedOrderProducts().first()

        (1..totalProductCount).forEach { i ->
            val productDomain = OrderProduct(
                id = null,
                productId = i.toLong(),
                quantity = baseProductDomain.quantity,
                price = BigDecimal.valueOf(100)
            )
            val productEntity = OrderProductJpaEntity.toJpaEntity(productDomain, savedOrderEntity)
            orderProductRepository.save(productEntity)
        }

        return savedOrderEntity
    }

    @Test
    fun `findById 호출 시 Order와 OrderProducts가 매핑된 도메인 모델을 반환해야 한다`() {
        // Given
        val savedEntity = saveTestOrder(userId = initialUserId, orderId = null, totalProductCount = 3)
        val orderId = savedEntity.id!!

        // When
        val order = orderQueryAdapter.findById(orderId)

        // Then
        order.id shouldBe orderId
        order.userId shouldBe initialUserId

        order.orderProducts shouldHaveSize 3
        order.orderProducts.first().productId shouldBe 1L
    }

    @Test
    fun `findById 호출 시 존재하지 않는 ID면 OrderNotFoundException을 던져야 한다`() {
        // Given
        val nonExistingOrderId = 999L

        // When & Then
        shouldThrow<OrderNotFoundException> {
            orderQueryAdapter.findById(nonExistingOrderId)
        }
    }

    @Test
    fun `findOrdersByUserId 호출 시 사용자 ID로 필터링되고 OrderProducts가 통합된 PagedResult를 반환해야 한다`() {
        // Given
        val targetUserId = 11L
        val otherUserId = 12L

        saveTestOrder(userId = targetUserId, orderId = null, totalProductCount = 3)
        saveTestOrder(userId = targetUserId, orderId = null, totalProductCount = 2)
        saveTestOrder(userId = targetUserId, orderId = null, totalProductCount = 4)
        saveTestOrder(userId = otherUserId, orderId = null, totalProductCount = 1)

        val pageQuery = PageQuery(pageNumber = 0, pageSize = 10, sortBy = "createdAt", direction = "DESC")

        // When
        val result = orderQueryAdapter.findOrdersByUserId(targetUserId, pageQuery)

        // Then
        result.totalElements shouldBe 3L
        result.content shouldHaveSize 3

        val orderWithThreeProducts = result.content.first()
        orderWithThreeProducts.orderProducts shouldHaveSize 3
        orderWithThreeProducts.userId shouldBe targetUserId
    }

    @Test
    fun `findOrdersByUserId 호출 시 정렬 파라미터가 올바르게 적용되어야 한다`() {
        // Given
        val targetUserId = 11L

        saveTestOrder(userId = targetUserId, orderId = null)
        saveTestOrder(userId = targetUserId, orderId = null)
        saveTestOrder(userId = targetUserId, orderId = null)

        val pageQuery = PageQuery(pageNumber = 0, pageSize = 10, sortBy = "id", direction = "DESC")

        // When
        val result = orderQueryAdapter.findOrdersByUserId(targetUserId, pageQuery)

        // Then
        result.content.first().id shouldBe 3L
        result.content.last().id shouldBe 1L
    }

    @Test
    fun `정렬 파라미터가 잘못되었을 때 createdAt 기준의 기본 정렬이 적용되어야 한다`() {
        // Given
        val targetUserId = 15L

        saveTestOrder(userId = targetUserId, orderId = null)
        saveTestOrder(userId = targetUserId, orderId = null)
        saveTestOrder(userId = targetUserId, orderId = null)

        val invalidDirectionQuery = PageQuery(pageNumber = 0, pageSize = 10, sortBy = "id", direction = "ascending")

        // When
        val result = orderQueryAdapter.findOrdersByUserId(targetUserId, invalidDirectionQuery)

        // Then
        result.content shouldHaveSize 3
        result.content.zipWithNext().forEach { (current, next) ->
            current.createdAt.shouldBeGreaterThanOrEqualTo(next.createdAt)
        }
    }
}
