package com.chobolevel.api.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class WarmupScheduler(
    private val restTemplate: RestTemplate,
) {

    @Scheduled(cron = "0 */30 * * * *")
    fun warmup() {
        restTemplate.getForEntity<String>("http://localhost:9565/api/v1/posts")
    }
}
