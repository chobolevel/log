package com.chobolevel.domain.entity.user

import com.chobolevel.domain.Pagination
import com.chobolevel.domain.entity.user.QUser.user
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.querydsl.core.types.OrderSpecifier
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import kotlin.jvm.Throws

@Component
class UserFinder(
    private val repository: UserRepository,
    private val customRepository: UserCustomRepository
) {

    @Throws(ApiException::class)
    fun findById(id: Long): User {
        return repository.findByIdOrNull(id) ?: throw ApiException(
            errorCode = ErrorCode.U001,
            status = HttpStatus.BAD_REQUEST,
            message = "회원 정보를 찾을 수 없습니다."
        )
    }

    @Throws(ApiException::class)
    fun findByEmailAndLoginType(email: String, loginType: UserLoginType): User {
        return repository.findByEmailAndLoginTypeAndResignedFalse(email, loginType) ?: throw ApiException(
            errorCode = ErrorCode.U001,
            status = HttpStatus.BAD_REQUEST,
            message = "회원 정보를 찾을 수 없습니다."
        )
    }

    @Throws(ApiException::class)
    fun findBySocialIdAndLoginType(socialId: String, loginType: UserLoginType): User {
        return repository.findBySocialIdAndLoginTypeAndResignedFalse(socialId, loginType) ?: throw ApiException(
            errorCode = ErrorCode.U001,
            status = HttpStatus.BAD_REQUEST,
            message = "회원 정보를 찾을 수 없습니다."
        )
    }

    fun existsByEmail(email: String): Boolean {
        return repository.existsByEmailAndResignedFalse(
            email = email,
        )
    }

    fun existsByNickname(nickname: String): Boolean {
        return repository.existsByNicknameAndResignedFalse(
            nickname = nickname
        )
    }

    fun existsByPhone(phone: String): Boolean {
        return repository.existsByPhoneAndResignedFalse(
            phone = phone
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
                UserOrderType.EMAIL_ASC -> user.email.asc()
                UserOrderType.EMAIL_DESC -> user.email.desc()
                UserOrderType.NICKNAME_ASC -> user.nickname.asc()
                UserOrderType.NICKNAME_DESC -> user.nickname.desc()
            }
        }.toTypedArray()
    }
}
