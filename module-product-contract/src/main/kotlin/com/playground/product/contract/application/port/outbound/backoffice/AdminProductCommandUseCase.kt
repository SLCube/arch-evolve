package com.playground.product.contract.application.port.outbound.backoffice

import com.playground.product.contract.application.port.outbound.backoffice.command.AdminProductSaveCommand
import com.playground.product.contract.application.port.outbound.backoffice.command.AdminProductUpdateCommand

interface AdminProductCommandUseCase {
    fun saveProduct(command: AdminProductSaveCommand)
    fun updateProduct(command: AdminProductUpdateCommand)
}