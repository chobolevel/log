package com.chobolevel.api.aop

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class MetricsAspect(
    private val meterRegistry: MeterRegistry
) {

    @Around("execution(* com.chobolevel.api.controller.v1.*.*.*(..))")
    fun methodExecution(pjp: ProceedingJoinPoint): Any {
        val timer = Timer.start(meterRegistry)
        try {
            return pjp.proceed()
        } finally {
            timer.stop(meterRegistry.timer("controller.execution.time", "method", pjp.signature.name))
        }
    }

}
