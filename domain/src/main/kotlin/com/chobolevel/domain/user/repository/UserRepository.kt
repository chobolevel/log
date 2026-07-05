package com.chobolevel.domain.user.repository

import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.entity.UserLoginType
import com.chobolevel.domain.user.entity.UserOrderType
import com.chobolevel.domain.user.vo.UserQueryFilter

interface UserRepository {

    fun save(user: User): User

    fun searchUsers(
        queryFilter: UserQueryFilter,
        paging: Paging,
        orderTypes: List<UserOrderType>
    ): List<User>

    fun searchUsersCount(queryFilter: UserQueryFilter): Long

    fun findById(id: Long): User

    fun findByEmailAndLoginType(email: String, loginType: UserLoginType): User?

    fun findBySocialIdAndLoginType(socialId: String, loginType: UserLoginType): User?

    fun findByIds(ids: List<Long>): List<User>

    fun existsByEmail(email: String): Boolean

    fun existsByNickname(nickname: String): Boolean
}
