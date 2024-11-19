package com.chobolevel.api.warmer

import com.chobolevel.api.service.post.PostService
import com.chobolevel.api.service.user.UserService
import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.PostQueryFilter
import com.chobolevel.domain.entity.user.UserQueryFilter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PostExactlyOnceRunWarmer(
    private val postService: PostService,
    private val userService: UserService
) : ExactlyOnceRunWarmer() {

    private val logger = LoggerFactory.getLogger(PostExactlyOnceRunWarmer::class.java)

    override suspend fun doRun() {
        logger.info("==================================== warm up started ====================================")
        postService.searchPosts(
            queryFilter = PostQueryFilter(null, null, null),
            pagination = Pagination(0, 50),
            orderTypes = null
        )
        userService.searchUserList(
            queryFilter = UserQueryFilter(null, null, null, null, null, null),
            pagination = Pagination(0, 50),
            orderTypes = null
        )
        logger.info("==================================== warm up ended ====================================")
    }
}
