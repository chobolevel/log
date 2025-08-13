package com.chobolevel.api.service.post

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.post.CreatePostRequestDto
import com.chobolevel.api.dto.post.PostResponseDto
import com.chobolevel.api.dto.post.UpdatePostRequestDto
import com.chobolevel.api.service.post.converter.PostConverter
import com.chobolevel.api.service.post.converter.PostImageConverter
import com.chobolevel.api.service.post.updater.PostUpdatable
import com.chobolevel.api.service.post.validator.UpdatePostValidatable
import com.chobolevel.domain.entity.post.Post
import com.chobolevel.domain.entity.post.PostFinder
import com.chobolevel.domain.entity.post.PostOrderType
import com.chobolevel.domain.entity.post.PostQueryFilter
import com.chobolevel.domain.entity.post.PostRepository
import com.chobolevel.domain.entity.post.tag.PostTag
import com.chobolevel.domain.entity.tag.Tag
import com.chobolevel.domain.entity.tag.TagFinder
import com.chobolevel.domain.entity.user.User
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class PostService(
    private val repository: PostRepository,
    private val finder: PostFinder,
    private val userFinder: UserFinder,
    private val tagFinder: TagFinder,
    private val converter: PostConverter,
    private val postImageConverter: PostImageConverter,
    private val updateValidators: List<UpdatePostValidatable>,
    private val updaters: List<PostUpdatable>,
    private val redisTemplate: RedisTemplate<String, PostResponseDto>
) {

    @Transactional
    fun createPost(userId: Long, request: CreatePostRequestDto): Long {
        val writer: User = userFinder.findById(userId)
        val post: Post = converter.convert(request).also { post ->
            post.setBy(writer)

            val tags: List<Tag> = tagFinder.findByIds(request.tagIds)
            // 뭔가 조잡한 느낌
            tags.forEach { tag ->
                PostTag().also { postTag ->
                    postTag.setBy(post)
                    postTag.setBy(tag)
                }
            }

            if (request.thumbNailIMage != null) {
                postImageConverter.convert(request.thumbNailIMage).also {
                    it.setBy(post)
                }
            }
        }
        // write caching pattern(write around)
        return repository.save(post).id!!
    }

    @Transactional(readOnly = true)
    fun searchPosts(
        queryFilter: PostQueryFilter,
        pagination: Pagination,
        orderTypes: List<PostOrderType>?
    ): PaginationResponseDto {
        val posts: List<Post> = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount: Long = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = converter.convert(entities = posts),
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchPost(postId: Long): PostResponseDto {
        val cachingKey: String = generateCachingKey(postId)
        val cachedPost: PostResponseDto? = redisTemplate.opsForValue().get(cachingKey)
        // read caching pattern(cache(look) aside)
        when (cachedPost) {
            // cache miss
            null -> {
                val post: Post = finder.findById(postId)
                val convertedPost: PostResponseDto = converter.convert(post)
                redisTemplate.opsForValue().set(cachingKey, convertedPost, 10, TimeUnit.MINUTES)
                return convertedPost
            }
            // cache hit
            else -> return cachedPost
        }
    }

    @Transactional
    fun updatePost(userId: Long, postId: Long, request: UpdatePostRequestDto): Long {
        updateValidators.forEach { it.validate(request) }
        val post: Post = finder.findById(postId)
        validateWriter(
            userId = userId,
            post = post
        )
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, post) }
        redisTemplate.delete(generateCachingKey(postId))
        return post.id!!
    }

    @Transactional
    fun deletePost(userId: Long, postId: Long): Boolean {
        val post: Post = finder.findById(postId)
        validateWriter(
            userId = userId,
            post = post
        )
        post.delete()
        redisTemplate.delete(generateCachingKey(postId))
        return true
    }

    private fun generateCachingKey(postId: Long): String {
        return "post:$postId"
    }

    private fun validateWriter(userId: Long, post: Post) {
        if (post.user!!.id != userId) {
            throw ApiException(
                errorCode = ErrorCode.POST_ONLY_ACCESS_WRITER,
                message = "작성자만 접근할 수 있습니다."
            )
        }
    }
}
