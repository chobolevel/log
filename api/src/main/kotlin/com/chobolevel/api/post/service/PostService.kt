package com.chobolevel.api.post.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.post.converter.PostConverter
import com.chobolevel.api.post.dto.CreatePostRequestDto
import com.chobolevel.api.post.dto.PostPageRequest
import com.chobolevel.api.post.dto.PostResponseDto
import com.chobolevel.api.post.dto.SearchPostRequest
import com.chobolevel.api.post.dto.UpdatePostRequestDto
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.api.post.updater.PostUpdatable
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.entity.PostOrderType
import com.chobolevel.domain.post.repository.PostRepository
import com.chobolevel.domain.post.tag.entity.PostTag
import com.chobolevel.domain.post.vo.PostQueryFilter
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.repository.TagRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class PostService(
    private val repository: PostRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val converter: PostConverter,
    private val postImageConverter: PostImageConverter,
    private val updaters: List<PostUpdatable>,
    private val redisTemplate: RedisTemplate<String, PostResponseDto>
) {

    @Transactional
    fun createPost(userId: Long, request: CreatePostRequestDto): Long {
        val writer: User = userRepository.findById(userId)
        val post: Post = converter.convert(request).also { post ->
            post.setBy(writer)

            val tags: List<Tag> = tagRepository.findByIds(request.tagIds)
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
        filter: SearchPostRequest,
        pageRequest: PostPageRequest
    ): PagingResponse {
        val queryFilter: PostQueryFilter = converter.convert(request = filter)
        val paging = Paging(page = pageRequest.page, size = pageRequest.size)
        val orderTypes: List<PostOrderType> = pageRequest.orderTypes
        val posts: List<Post> = repository.searchPosts(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = repository.searchPostsCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
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
                val post: Post = repository.findById(postId)
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
        val post: Post = repository.findById(postId)
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
        val post: Post = repository.findById(postId)
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
