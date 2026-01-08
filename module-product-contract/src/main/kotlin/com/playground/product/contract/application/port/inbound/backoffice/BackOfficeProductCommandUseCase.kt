package com.playground.product.contract.application.port.inbound.backoffice

import com.playground.product.contract.application.port.inbound.backoffice.command.AdminProductSaveCommand
import com.playground.product.contract.application.port.inbound.backoffice.command.AdminProductUpdateCommand

interface BackOfficeProductCommandUseCase {
    fun saveProduct(command: AdminProductSaveCommand)
    fun updateProduct(command: AdminProductUpdateCommand)
}