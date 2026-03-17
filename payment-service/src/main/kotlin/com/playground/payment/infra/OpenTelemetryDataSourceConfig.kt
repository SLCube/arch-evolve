package com.playground.payment.infra

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class OpenTelemetryDataSourceConfig(
    private val openTelemetryProvider: ObjectProvider<OpenTelemetry>,
) : BeanPostProcessor {
    override fun postProcessAfterInitialization(
        bean: Any,
        beanName: String,
    ): Any {
        if (bean is DataSource) {
            val openTelemetry = openTelemetryProvider.getIfAvailable() ?: return bean
            return JdbcTelemetry.create(openTelemetry).wrap(bean)
        }
        return bean
    }
}
