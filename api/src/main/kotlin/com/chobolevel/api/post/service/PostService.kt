package com.chobolevel.api.post.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.post.assembler.PostAssembler
import com.chobolevel.api.post.converter.PostConverter
import com.chobolevel.api.post.dto.CreatePostRequest
import com.chobolevel.api.post.dto.PostPagingRequest
import com.chobolevel.api.post.dto.PostResponse
import com.chobolevel.api.post.dto.SearchPostRequest
import com.chobolevel.api.post.dto.UpdatePostRequest
import com.chobolevel.api.post.image.converter.PostImageConverter
import com.chobolevel.api.post.updater.PostUpdatable
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.post.repository.PostRepository
import com.chobolevel.domain.post.vo.PostOrderType
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
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val tagRepository: TagRepository,
    private val postConverter: PostConverter,
    private val postImageConverter: PostImageConverter,
    private val postAssembler: PostAssembler,
    private val postUpdaters: List<PostUpdatable>,
    private val redisTemplate: RedisTemplate<String, PostResponse>
) {

    @Transactional
    fun createPost(userId: Long, request: CreatePostRequest): Long {
        val user: User = userRepository.findById(userId)
        val post: Post = postConverter.convert(request)
        val postThumbnailImage: PostImage? = request.thumbnailImage?.let { postImageConverter.convert(it) }
        val tags: List<Tag> = tagRepository.findByIds(request.tagIds)

        val assembledPost: Post = postAssembler.assemble(
            post = post,
            postThumbnailImage = postThumbnailImage,
            user = user,
            tags = tags
        )

        return postRepository.save(assembledPost).id!!
    }

    @Transactional(readOnly = true)
    fun searchPosts(
        filter: SearchPostRequest,
        pageRequest: PostPagingRequest
    ): PagingResponse {
        val queryFilter: PostQueryFilter = postConverter.convert(request = filter)
        val paging = Paging(page = pageRequest.page, size = pageRequest.size)
        val orderTypes: List<PostOrderType> = pageRequest.orderTypes
        val posts: List<Post> = postRepository.searchPosts(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = orderTypes
        )
        val totalCount: Long = postRepository.searchPostsCount(queryFilter)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = postConverter.convert(entities = posts),
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchPost(postId: Long): PostResponse {
        val cachingKey: String = generateCachingKey(postId)
        val cachedPost: PostResponse? = redisTemplate.opsForValue().get(cachingKey)
        // read caching pattern(cache(look) aside)
        when (cachedPost) {
            // cache miss
            null -> {
                val post: Post = postRepository.findById(postId)
                val convertedPost: PostResponse = postConverter.convert(post)
                redisTemplate.opsForValue().set(cachingKey, convertedPost, 10, TimeUnit.MINUTES)
                return convertedPost
            }
            // cache hit
            else -> return cachedPost
        }
    }

    @Transactional
    fun updatePost(userId: Long, postId: Long, request: UpdatePostRequest): Long {
        val post: Post = postRepository.findById(postId)
        validateWriter(
            userId = userId,
            post = post
        )
        postUpdaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, post) }
        redisTemplate.delete(generateCachingKey(postId))
        return post.id!!
    }

    @Transactional
    fun deletePost(userId: Long, postId: Long): Boolean {
        val post: Post = postRepository.findById(postId)
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
