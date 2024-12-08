package com.chobolevel.api.scheduler

import com.chobolevel.api.warmer.Warmer
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.InetAddress

@Component
class WarmScheduler(
    private val warmers: List<Warmer>,
) {

    private val logger = LoggerFactory.getLogger(WarmScheduler::class.java)

    @Async
    @Scheduled(cron = "0 */10 * * * *")
    fun postControllerWarmer() {
        val restTemplate = RestTemplate()
        val url = "http://${InetAddress.getLocalHost().hostAddress}:9565"
        logger.info("===== warm up started with $url ====")
        warmers.forEach { it.warm(url = url, restTemplate = restTemplate) }
        logger.info("===== warm up ended with $url ======")
    }
}
