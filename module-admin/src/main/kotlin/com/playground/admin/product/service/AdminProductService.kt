package com.playground.admin.product.service

import com.playground.product.contract.application.port.outbound.backoffice.AdminProductCommandUseCase
import com.playground.product.contract.application.port.outbound.backoffice.command.AdminProductSaveCommand
import com.playground.product.contract.application.port.outbound.backoffice.command.AdminProductUpdateCommand
import org.springframework.stereotype.Service

@Service
class AdminProductService(
    private val adminProductCommandUseCase: AdminProductCommandUseCase,
) {

    fun saveProduct(command: AdminProductSaveCommand) {
        adminProductCommandUseCase.saveProduct(command)
    }

    fun updateProduct(command: AdminProductUpdateCommand) {
        adminProductCommandUseCase.updateProduct(command)
    }
}