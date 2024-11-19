package com.chobolevel.api.scheduler

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class WarmupScheduler(
    private val restTemplate: RestTemplate,
    @Value("\${server.protocol}")
    private val serverProtocol: String,
    @Value("\${server.host}")
    private val serverHost: String,
) {

    @Scheduled(cron = "0 */30 * * * *")
    fun warmup() {
        restTemplate.getForEntity<String>("$serverProtocol://$serverHost/api/v1/posts")
    }
}
