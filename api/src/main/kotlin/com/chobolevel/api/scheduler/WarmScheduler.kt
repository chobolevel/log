package com.chobolevel.api.scheduler

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class WarmScheduler(
    private val restTemplate: RestTemplate,
    @Value("\${server.host}")
    private val host: String
) {

    private val logger = LoggerFactory.getLogger(WarmScheduler::class.java)

    @Scheduled(cron = "0 */30 * * * *")
    fun postControllerWarmer() {
        logger.info("===== warm up scheduler started with $host ====")
        restTemplate.getForEntity<String>("$host/api/v1/posts")
        restTemplate.getForEntity<String>("$host/api/v1/posts/1")
        logger.info("===== warm up scheduler ended with $host ======")
    }
}
