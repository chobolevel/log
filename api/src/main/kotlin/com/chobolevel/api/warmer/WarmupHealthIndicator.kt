//package com.chobolevel.api.warmer
//
//import kotlinx.coroutines.reactor.mono
//import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator
//import org.springframework.boot.actuate.health.Health
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Mono
//
//// AbstractReactiveHealthIndicator 상속해 웜업 상태 정보를 제공
//@Component
//class WarmupHealthIndicator(
//    private val warmer: Warmer
//) : AbstractReactiveHealthIndicator() {
//
//    override fun doHealthCheck(builder: Health.Builder): Mono<Health> {
//        return mono {
//            warmer.run()
//            val health = builder.also {
//                if (warmer.isDone) {
//                    it.up()
//                } else {
//                    it.down()
//                }
//            }.build()
//
//            return@mono health
//        }
//    }
//}
