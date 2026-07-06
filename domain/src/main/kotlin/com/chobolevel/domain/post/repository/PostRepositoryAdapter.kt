package com.chobolevel.domain.post.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.entity.QPost.post
import com.chobolevel.domain.post.vo.PostOrderType
import com.chobolevel.domain.post.vo.PostQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class PostRepositoryAdapter(
    private val postJpaRepository: PostJpaRepository,
    private val postQuerydslRepository: PostQuerydslRepository
) : PostRepository {

    override fun save(post: Post): Post {
        return postJpaRepository.save(post)
    }

    override fun findById(id: Long): Post {
        return postJpaRepository.findByIdAndDeletedFalse(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "해당 게시글을 찾을 수 없습니다."
        )
    }

    override fun searchPosts(
        queryFilter: PostQueryFilter,
        paging: Paging,
        orderTypes: List<PostOrderType>
    ): List<Post> {
        return postQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchPostsCount(queryFilter: PostQueryFilter): Long {
        return postQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<PostOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                PostOrderType.CREATED_AT_ASC -> post.createdAt.asc()
                PostOrderType.CREATED_AT_DESC -> post.createdAt.desc()
                PostOrderType.UPDATED_AT_ASC -> post.updatedAt.asc()
                PostOrderType.UPDATED_AT_DESC -> post.updatedAt.desc()
            }
        }.toTypedArray()
    }
}
