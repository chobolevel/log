package com.chobolevel.api.service.user

import com.chobolevel.api.dto.common.PaginationResponseDto
import com.chobolevel.api.dto.user.ChangeUserPasswordRequest
import com.chobolevel.api.dto.user.CreateUserRequestDto
import com.chobolevel.api.dto.user.UpdateUserRequestDto
import com.chobolevel.api.dto.user.UserResponseDto
import com.chobolevel.api.service.user.converter.UserConverter
import com.chobolevel.api.service.user.updater.UserUpdatable
import com.chobolevel.api.service.user.validator.CreateUserValidatable
import com.chobolevel.api.service.user.validator.UpdateUserValidatable
import com.chobolevel.domain.entity.user.UserFinder
import com.chobolevel.domain.entity.user.UserOrderType
import com.chobolevel.domain.entity.user.UserQueryFilter
import com.chobolevel.domain.entity.user.UserRepository
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import com.scrimmers.domain.dto.common.Pagination
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val repository: UserRepository,
    private val finder: UserFinder,
    private val converter: UserConverter,
    private val createValidators: List<CreateUserValidatable>,
    private val updateValidators: List<UpdateUserValidatable>,
    private val updaters: List<UserUpdatable>,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional
    fun createUser(request: CreateUserRequestDto): Long {
        createValidators.forEach { it.validate(request) }
        if (finder.existsByEmail(request.email)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 이메일입니다."
            )
        }
        if (finder.existsByNickname(request.nickname)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 닉네임입니다."
            )
        }
        if (finder.existsByPhone(request.phone)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "이미 존재하는 전화번호입니다."
            )
        }
        val user = converter.convert(request)
        return repository.save(user).id!!
    }

    @Transactional(readOnly = true)
    fun searchUserList(
        queryFilter: UserQueryFilter,
        pagination: Pagination,
        orderTypes: List<UserOrderType>?
    ): PaginationResponseDto {
        val userList = finder.search(queryFilter, pagination, orderTypes)
        val totalCount = finder.searchCount(queryFilter)
        return PaginationResponseDto(
            skipCount = pagination.offset,
            limitCount = pagination.limit,
            data = userList.map { converter.convert(it) },
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchUser(id: Long): UserResponseDto {
        val user = finder.findById(id)
        return converter.convert(user)
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequestDto): Long {
        updateValidators.forEach { it.validate(request) }
        val user = finder.findById(id)
        updaters.sortedBy { it.order() }.forEach { it.markAsUpdate(request, user) }
        return user.id!!
    }

    @Transactional
    fun changePassword(id: Long, request: ChangeUserPasswordRequest): Long {
        val user = finder.findById(id)
        if (!passwordEncoder.matches(request.curPassword, user.password)) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "현재 비밀번호가 일치하지 않습니다."
            )
        }
        if (request.curPassword == request.newPassword) {
            throw ApiException(
                errorCode = ErrorCode.INVALID_PARAMETER,
                status = HttpStatus.BAD_REQUEST,
                message = "현재 비밀번호와 같은 비밀번호로 변경할 수 없습니다."
            )
        }
        user.password = passwordEncoder.encode(request.newPassword)
        return user.id!!
    }

    @Transactional
    fun resignUser(id: Long): Boolean {
        val user = finder.findById(id)
        repository.delete(user)
        return true
    }
}
