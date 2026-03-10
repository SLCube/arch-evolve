package com.playground.delivery.consumer.support

import com.playground.delivery.common.log.utils.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class KafkaConsumerAspect {
    private val log = logger()

    @Around("@annotation(com.playground.delivery.consumer.support.KafkaConsumerHandler)")
    fun handleConsumer(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.toShortString()
        try {
            log.info("==> Kafka Consumer 시작: {}", methodName)
            val result = joinPoint.proceed()
            log.info("<== Kafka Consumer 완료: {}", methodName)
            return result
        } catch (e: Exception) {
            log.error("Kafka Consumer 처리 실패 [{}]", methodName, e)
            throw e
        }
    }
}
