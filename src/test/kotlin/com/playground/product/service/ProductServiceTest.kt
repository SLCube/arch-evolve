package com.playground.product.service

import com.playground.product.application.port.`in`.ProductUseCase
import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Suppress("NonAsciiCharacters")
@SpringBootTest
class ProductServiceTest(
    @param:Autowired private val productUseCase: ProductUseCase,
    @param:Autowired private val productRepository: ProductRepository,
) {
    private var productId: Long = 0L

    @BeforeEach
    fun setUp() {
        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "테스트 상품", stock = 100, price = 10000L))
        productId = productJpaEntity.id!!
    }

    @AfterEach
    fun tearDown() {
        productRepository.deleteAll()
    }

    @Test
    fun `동시에 100개의 재고를 차감하면, 최종 재고는 0이 된다`() {
        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    productUseCase.decreaseStock(DecreaseStockCommand(productId, 1))
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        val finalProduct = productRepository.findById(productId).get()
        finalProduct.stock shouldBe 0
    }
}
