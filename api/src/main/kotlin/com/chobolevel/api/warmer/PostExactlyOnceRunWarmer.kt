package com.chobolevel.api.warmer

import com.chobolevel.api.controller.v1.post.PostController
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PostExactlyOnceRunWarmer(
    private val postController: PostController
) : ExactlyOnceRunWarmer() {

    private val logger = LoggerFactory.getLogger(PostExactlyOnceRunWarmer::class.java)

    override suspend fun doRun() {
        postController.searchPosts(
            tagId = null,
            title = null,
            content = null,
            skipCount = null,
            limitCount = null,
            orderTypes = null
        )
    }
}
