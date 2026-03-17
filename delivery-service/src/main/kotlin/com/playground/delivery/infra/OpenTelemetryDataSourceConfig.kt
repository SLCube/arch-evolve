package com.playground.delivery.infra

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
@ConditionalOnClass(DataSource::class)
class OpenTelemetryDataSourceConfig {

    @Bean
    @Primary
    fun otelDataSource(
        @Qualifier("dataSource") dataSource: DataSource,
        openTelemetry: OpenTelemetry,
    ): DataSource = JdbcTelemetry.create(openTelemetry).wrap(dataSource)
}
