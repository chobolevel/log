package com.chobolevel.domain.tag.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.tag.entity.QTag.tag
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.tag.entity.TagOrderType
import com.chobolevel.domain.tag.vo.TagQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class TagRepositoryAdapter(
    private val tagJpaRepository: TagJpaRepository,
    private val tagQuerydslRepository: TagQuerydslRepository
) : TagRepository {

    override fun save(tag: Tag): Tag {
        return tagJpaRepository.save(tag)
    }

    override fun delete(tag: Tag) {
        tagJpaRepository.delete(tag)
    }

    override fun findById(id: Long): Tag {
        return tagJpaRepository.findByIdOrNull(id) ?: throw ApiException(
            errorCode = ErrorCode.INVALID_PARAMETER,
            status = HttpStatus.BAD_REQUEST,
            message = "게시글 태그를 찾을 수 없습니다."
        )
    }

    override fun findByIds(ids: List<Long>): List<Tag> {
        return tagJpaRepository.findByIdInAndDeletedFalse(ids)
    }

    override fun searchTags(
        queryFilter: TagQueryFilter,
        paging: Paging,
        orderTypes: List<TagOrderType>
    ): List<Tag> {
        return tagQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            paging = paging,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchTagsCount(queryFilter: TagQueryFilter): Long {
        return tagQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    private fun List<TagOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                TagOrderType.ORDER_ASC -> tag.order.asc()
                TagOrderType.ORDER_DESC -> tag.order.desc()
            }
        }.toTypedArray()
    }
}
