package com.playground.product.fixture.application.query

import com.playground.product.application.port.inbound.query.ProductGetQuery

object ProductQueryTestFixture {

    fun mockProductQuery(
        id: Long = 1L
    ) = ProductGetQuery(
        id = id,
    )
}