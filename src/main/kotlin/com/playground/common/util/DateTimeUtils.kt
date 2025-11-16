package com.playground.common.util

import java.time.format.DateTimeFormatter

object DateTimeUtils {
    val API_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}
