package com.playground.admin.product.controller

import com.playground.admin.product.controller.mapper.toCommand
import com.playground.admin.product.controller.request.AdminProductSaveRequestDto
import com.playground.admin.product.controller.request.AdminProductUpdateRequestDto
import com.playground.admin.product.service.AdminProductService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/products")
class AdminProductController(
    private val adminProductService: AdminProductService,
) {
    @PostMapping
    fun save(
        @RequestBody @Valid requestDto: AdminProductSaveRequestDto,
    ) {
        adminProductService.saveProduct(requestDto.toCommand())
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid requestDto: AdminProductUpdateRequestDto,
    ) {
        adminProductService.updateProduct(requestDto.toCommand(id))
    }
}