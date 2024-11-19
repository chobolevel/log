package com.chobolevel.api.scheduler

import com.chobolevel.api.controller.v1.post.PostController
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WarmScheduler(
    private val postController: PostController
) {

    @Scheduled(cron = "0 */30 * * * *")
    fun postControllerWarmer() {
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
