package com.playground.product.presentation.annotation

import com.playground.product.presentation.web.ProductController
import com.playground.support.security.config.TestSecurityConfig
import com.playground.product.presentation.config.ProductControllerTestConfig
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(ProductController::class)
@Import(ProductControllerTestConfig::class, TestSecurityConfig::class)
annotation class ProductControllerSliceTest
