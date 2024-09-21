package com.chobolevel.api.service.post

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.post.CreatePostRequestDto
import com.chobolevel.api.dto.post.PostResponseDto
import com.chobolevel.api.dto.post.UpdatePostRequestDto
import com.chobolevel.api.service.post.converter.PostConverter
import com.chobolevel.api.service.post.converter.PostImageConverter
import com.chobolevel.api.service.post.updater.PostUpdatable
import com.chobolevel.api.service.post.validator.UpdatePostValidatable
import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.post.PostFinder
import com.chobolevel.domain.entity.post.PostOrderType
import com.chobolevel.domain.entity.post.PostQueryFilter
import com.chobolevel.domain.entity.post.PostRepository
import com.chobolevel.domain.entity.post.tag.PostTag
import com.chobolevel.domain.entity.tag.TagFinder
import com.chobolevel.domain.entity.user.UserFinder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val repository: PostRepository,
    private val finder: PostFinder,
    private val userFinder: UserFinder,
    private val tagFinder: TagFinder,
    private val converter: PostConverter,
    private val postImageConverter: PostImageConverter,
    private val updateValidators: List<UpdatePostValidatable>,
    private val updaters: List<PostUpdatable>
) {

    @Transactional
    fun createPost(userId: Long, request: CreatePostRequestDto): Long {
        val foundUser = userFinder.findById(userId)
        val post = converter.convert(request).also { post ->
            post.setBy(foundUser)
            request.tagIds.forEach { tagId ->
                val postTag = PostTag()
                val tag = tagFinder.findById(tagId)
                postTag.setBy(post)
                postTag.setBy(tag)
            }
            if (request.thumbNailIMage != null) {
                postImageConverter.convert(request.thumbNailIMage).also {
                    it.setBy(post)
                }
            }
        }
        return repository.save(post).id!!
    }

    @Transactional(readOnly = true)
    fun searchPosts(
        queryFilter: PostQueryFilter,
        pagination: Pagination,
        orderTypes: List<PostOrderType>?
    ): PaginationResponseDto {
        val posts = finder.search(
            queryFilter = queryFilter,
            pagination = pagination,
            orderTypes = orderTypes
        )
        val totalCount = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.skip,
            limitCount = pagination.limit,
            data = posts.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchPost(postId: Long): PostResponseDto {
        val post = finder.findById(postId)
        return converter.convert(post)
    }

    @Transactional
    fun updatePost(userId: Long, postId: Long, request: UpdatePostRequestDto): Long {
        updateValidators.forEach { it.validate(request) }
        val post = finder.findByIdAndUserId(
            id = postId,
            userId = userId
        )
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, post) }
        return post.id!!
    }

    @Transactional
    fun deletePost(userId: Long, postId: Long): Boolean {
        val post = finder.findByIdAndUserId(
            id = postId,
            userId = userId
        )
        post.delete()
        return true
    }
}
