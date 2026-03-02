package com.playground.order.consumer.support

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class KafkaConsumerAspect {
    private val log = LoggerFactory.getLogger(javaClass)

    @Around("@annotation(com.playground.order.consumer.support.KafkaConsumerHandler)")
    fun handleConsumer(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.toShortString()
        try {
            log.debug("==> Kafka Consumer 시작: {}", methodName)
            val result = joinPoint.proceed()
            log.debug("<== Kafka Consumer 완료: {}", methodName)
            return result
        } catch (e: Exception) {
            log.error("Kafka Consumer 처리 실패 [{}]", methodName, e)
            throw e
        }
    }
}
