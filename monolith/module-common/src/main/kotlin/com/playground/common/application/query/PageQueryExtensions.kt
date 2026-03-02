package com.playground.common.application.query

import org.springframework.data.domain.Sort

fun PageQuery.toSort(defaultField: String = "createdAt"): Sort {
    val sortBy = this.sortBy
    val direction = this.direction

    if (sortBy.isNullOrBlank() || direction.isNullOrBlank()) {
        return Sort.by(Sort.Direction.DESC, defaultField)
    }

    val directionEnum =
        runCatching {
            Sort.Direction.valueOf(direction.uppercase())
        }.getOrElse {
            Sort.Direction.DESC
        }

    return Sort.by(directionEnum, sortBy)
}
