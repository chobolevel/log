package com.chobolevel.api.scheduler

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.net.InetAddress

@Component
class WarmScheduler(
    private val restTemplate: RestTemplate,
) {

    private val logger = LoggerFactory.getLogger(WarmScheduler::class.java)

    @Async
    @Scheduled(cron = "0 */10 * * * *")
    fun postControllerWarmer() {
        val url = "http://${InetAddress.getLocalHost().hostAddress}:9565"
        logger.info("===== warm up started with $url ====")
        restTemplate.getForEntity<String>("$url/api/v1/posts")
        restTemplate.getForEntity<String>("$url/api/v1/posts/1")
        logger.info("===== warm up ended with $url ======")
    }
}
