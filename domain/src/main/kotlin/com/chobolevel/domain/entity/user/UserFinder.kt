package com.chobolevel.domain.entity.user

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.user.QUser.user
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UserFinder(
    private val repository: UserRepository,
    private val customRepository: UserCustomRepository
) {

    fun findById(id: Long): User {
        return repository.findByIdOrNull(id) ?: throw ApiException(
            errorCode = ErrorCode.U001,
            status = HttpStatus.BAD_REQUEST,
            message = "회원 정보를 찾을 수 없습니다."
        )
    }

    fun search(queryFilter: UserQueryFilter, pagination: Pagination, orderTypes: List<UserOrderType>?): List<User> {
        val orderSpecifiers = orderSpecifiers(orderTypes ?: emptyList())
        return customRepository.searchByPredicates(queryFilter.toPredicates(), pagination, orderSpecifiers)
    }

    fun searchCount(queryFilter: UserQueryFilter): Long {
        return customRepository.countByPredicates(queryFilter.toPredicates())
    }

    private fun orderSpecifiers(orderTypes: List<UserOrderType>): Array<OrderSpecifier<*>> {
        return orderTypes.map {
            when (it) {
                UserOrderType.CREATED_AT_ASC -> user.createdAt.asc()
                UserOrderType.CREATED_AT_DESC -> user.createdAt.desc()
                UserOrderType.UPDATED_AT_ASC -> user.updatedAt.asc()
                UserOrderType.UPDATED_AT_DESC -> user.updatedAt.desc()
            }
        }.toTypedArray()
    }
}
