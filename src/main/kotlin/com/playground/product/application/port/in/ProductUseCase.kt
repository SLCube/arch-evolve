package com.playground.product.application.port.`in`

import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import com.playground.product.application.port.`in`.command.SaveProductCommand
import com.playground.product.application.port.`in`.command.UpdateProductCommand
import com.playground.product.application.port.`in`.query.GetProductQuery
import com.playground.product.domain.Product

interface ProductUseCase {
    fun saveProduct(command: SaveProductCommand): Product

    fun updateProduct(command: UpdateProductCommand): Product

    fun getProduct(query: GetProductQuery): Product

    fun getAllProducts(): List<Product>

    fun decreaseStock(command: DecreaseStockCommand): Product
}
