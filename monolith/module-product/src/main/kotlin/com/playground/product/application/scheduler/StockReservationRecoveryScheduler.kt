package com.playground.product.application.scheduler

import com.playground.order.contract.port.OrderStockRecoveryPort
import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.application.service.StockReservationRecoveryService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StockReservationRecoveryScheduler(
    private val stock: StockCachePort,
    private val orders: ObjectProvider<OrderStockRecoveryPort>,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        if (orders.ifAvailable != null) stock.rebuildReservationRecoveryIndex()
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    fun recover() {
        val port = orders.ifAvailable ?: return // 상품 모듈 단독 테스트에는 주문 어댑터가 없다.
        try {
            StockReservationRecoveryService(stock, port).recover()
        } catch (exception: Exception) {
            logger.error("재고 예약 복구 배치 실패", exception)
        }
    }
}
