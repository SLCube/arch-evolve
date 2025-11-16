package com.playground.product.application.port.inbound

import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.SaveProductCommand
import com.playground.product.application.port.inbound.command.UpdateProductCommand
import com.playground.product.application.port.inbound.query.GetProductQuery
import com.playground.product.domain.model.Product

interface ProductUseCase {
    fun saveProduct(command: SaveProductCommand): Product

    fun updateProduct(command: UpdateProductCommand): Product

    fun getProduct(query: GetProductQuery): Product

    fun getAllProducts(): List<Product>

    fun decreaseStock(command: DecreaseStockCommand): Product
}
