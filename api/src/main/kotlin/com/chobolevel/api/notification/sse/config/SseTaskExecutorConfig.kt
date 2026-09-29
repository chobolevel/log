package com.chobolevel.api.notification.sse.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class SseTaskExecutorConfig {

    // 느린 클라이언트로의 emitter.send()가 Kafka 컨슈머 스레드를 막지 않도록 전용 스레드풀로 분리한다.
    // 큐까지 가득 찰 정도로 느린 연결이 몰리면(극단적 상황) CallerRunsPolicy로 호출 스레드에서 직접
    // 처리하게 해서 자연스러운 backpressure를 준다 — 무제한으로 쌓이거나 조용히 유실되는 것보다 낫다.
    @Bean("sseTaskExecutor")
    fun sseTaskExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 16
            queueCapacity = 200
            setThreadNamePrefix("sse-dispatch-")
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }
    }
}
