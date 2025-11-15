package com.playground.common.log.aop

import com.playground.common.log.utils.logger
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import java.util.Optional

@Aspect
@Component
class LoggerAspect {

    private val log = logger()

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    fun controller() {
        // 이 메소드는 Pointcut을 정의하는 데 사용됩니다.
    }

    @Pointcut("execution(* com.playground..*Service.*(..))")
    fun service() {
        // 이 메소드는 Pointcut을 정의하는 데 사용됩니다.
    }

    @Pointcut("execution(* com.playground..*Adapter.*(..))")
    fun adapter() {
        // 이 메소드는 Pointcut을 정의하는 데 사용됩니다.
    }

    @Before("controller() || service() || adapter()")
    fun logMethodStart(joinPoint: JoinPoint) {
        val signature = joinPoint.signature
        val methodName = signature.toShortString()
        val args = joinPoint.args.joinToString(", ")
        log.debug("==> Method Start: {} with args: [{}]", methodName, args)
    }

    @AfterReturning(pointcut = "controller() || service() || adapter()", returning = "result")
    fun logMethodEnd(joinPoint: JoinPoint, result: Any?) {
        val signature = joinPoint.signature
        val methodName = signature.toShortString()

        val resultValue = if (result is Optional<*>) {
            result.map { it.toString() }.orElse("empty")
        } else {
            result.toString()
        }

        log.debug("<== Method End: {} with result: [{}]", methodName, resultValue)
    }
}