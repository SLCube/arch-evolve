package com.playground.common.application.query

data class PageQuery(
    val pageNumber: Int,
    val pageSize: Int,
    val sortBy: String? = null,
    val direction: String? = null,
)
