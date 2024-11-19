package com.chobolevel.api

import com.chobolevel.api.controller.v1.post.PostController
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class ApiApplicationListener(
    private val postController: PostController
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ApiApplication::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info("===== warm up started ====")
        postController.searchPosts(
            tagId = null,
            title = null,
            content = null,
            skipCount = null,
            limitCount = null,
            orderTypes = null
        )
        logger.info("===== warm up ended ======")
    }
}
