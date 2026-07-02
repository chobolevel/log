package com.chobolevel.domain.user.repository

import com.chobolevel.domain.common.dto.Pagination
import com.chobolevel.domain.common.exception.ApiException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.user.entity.QUser.user
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.entity.UserLoginType
import com.chobolevel.domain.user.entity.UserOrderType
import com.chobolevel.domain.user.vo.UserQueryFilter
import com.querydsl.core.types.OrderSpecifier
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository,
    private val userQuerydslRepository: UserQuerydslRepository
) : UserRepository {

    override fun save(user: User): User {
        return userJpaRepository.save(user)
    }

    override fun searchUsers(
        queryFilter: UserQueryFilter,
        pagination: Pagination,
        orderTypes: List<UserOrderType>
    ): List<User> {
        return userQuerydslRepository.searchByPredicates(
            predicates = queryFilter.toPredicates(),
            pagination = pagination,
            orderSpecifiers = orderTypes.toOrderSpecifiers()
        )
    }

    override fun searchUsersCount(queryFilter: UserQueryFilter): Long {
        return userQuerydslRepository.countByPredicates(predicates = queryFilter.toPredicates())
    }

    override fun findById(id: Long): User {
        return userJpaRepository.findByIdOrNull(id) ?: throw ApiException(
            errorCode = ErrorCode.USER_NOT_FOUND,
            status = HttpStatus.BAD_REQUEST,
            message = "회원 정보를 찾을 수 없습니다."
        )
    }

    override fun findByEmailAndLoginType(
        email: String,
        loginType: UserLoginType
    ): User? {
        return userJpaRepository.findByEmailAndLoginTypeAndResignedFalse(email, loginType)
    }

    override fun findBySocialIdAndLoginType(
        socialId: String,
        loginType: UserLoginType
    ): User? {
        return userJpaRepository.findBySocialIdAndLoginTypeAndResignedFalse(socialId, loginType)
    }

    override fun findByIds(ids: List<Long>): List<User> {
        return userJpaRepository.findByIdInAndResignedFalse(ids)
    }

    override fun existsByEmail(email: String): Boolean {
        return userJpaRepository.existsByEmailAndResignedFalse(email)
    }

    override fun existsByNickname(nickname: String): Boolean {
        return userJpaRepository.existsByNicknameAndResignedFalse(nickname)
    }

    private fun List<UserOrderType>.toOrderSpecifiers(): Array<OrderSpecifier<*>> {
        return this.map {
            when (it) {
                UserOrderType.CREATED_AT_ASC -> user.createdAt.asc()
                UserOrderType.CREATED_AT_DESC -> user.createdAt.desc()
                UserOrderType.EMAIL_ASC -> user.email.asc()
                UserOrderType.EMAIL_DESC -> user.email.desc()
                UserOrderType.NICKNAME_ASC -> user.nickname.asc()
                UserOrderType.NICKNAME_DESC -> user.nickname.desc()
            }
        }.toTypedArray()
    }
}
