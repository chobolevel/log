package com.chobolevel.api.scheduler

import com.chobolevel.api.warmer.Warmer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.InetAddress

@Profile("production")
@Component
class WarmScheduler(
    private val warmers: List<Warmer>,
    @Qualifier("baseRestTemplate")
    private val restTemplate: RestTemplate,
) {

    private val logger = LoggerFactory.getLogger(WarmScheduler::class.java)

    @Async
    @Scheduled(cron = "0 */10 * * * *")
    fun postControllerWarmer() {
        val url = "http://${InetAddress.getLocalHost().hostAddress}:9565"
        logger.info("===== warm up started with $url ====")
        warmers.forEach {
            try {
                it.warm(url = url, restTemplate = restTemplate)
            } catch (e: Exception) {
                logger.error("error caused by ${it.javaClass.name} during warm up", e)
            }
        }
        logger.info("===== warm up ended with $url ======")
    }
}
