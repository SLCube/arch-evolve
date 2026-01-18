package com.playground.product.application.port.inbound

import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.ProductSaveCommand
import com.playground.product.application.port.inbound.command.ProductUpdateCommand
import com.playground.product.application.port.inbound.query.ProductGetQuery
import com.playground.product.domain.model.Product

interface ProductUseCase {
    fun saveProduct(command: ProductSaveCommand): Product

    fun updateProduct(command: ProductUpdateCommand): Product

    fun getProduct(query: ProductGetQuery): Product

    fun getAllProducts(): List<Product>

    fun decreaseStock(command: DecreaseStockCommand): Long
}
