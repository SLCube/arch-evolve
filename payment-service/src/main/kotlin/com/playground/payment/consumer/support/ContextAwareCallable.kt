package com.playground.payment.consumer.support

import io.opentelemetry.context.Context
import org.slf4j.MDC
import java.util.concurrent.Callable

class ContextAwareCallable<T>(
    private val callable: Callable<T>,
) : Callable<T> {
    private val mdc = MDC.getCopyOfContextMap() ?: emptyMap()
    private val otelContext = Context.current()

    override fun call(): T =
        otelContext.makeCurrent().use {
            MDC.clear()
            MDC.setContextMap(mdc.toMutableMap())
            try {
                callable.call()
            } finally {
                MDC.clear()
            }
        }
}
