package com.chobolevel.api.user.follow.service

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.api.user.follow.converter.UserFollowConverter
import com.chobolevel.api.user.follow.dto.SearchUserFollowerRequest
import com.chobolevel.api.user.follow.dto.SearchUserFollowingRequest
import com.chobolevel.api.user.follow.dto.UserFollowResponse
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.user.follow.entity.UserFollow
import com.chobolevel.domain.user.follow.repository.UserFollowRepository
import com.chobolevel.domain.user.follow.vo.UserFollowQueryFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserFollowQueryService(
    private val userFollowRepository: UserFollowRepository,
    private val userFollowConverter: UserFollowConverter,
    private val cacheProvider: CacheProvider,
) {

    // 팔로워 수 — 커맨드 시점에 낙관적으로 갱신되는 캐시라 DB COUNT보다 오히려 최신 값에 가깝다
    @Transactional(readOnly = true)
    fun fetchFollowerCount(userId: Long): Long {
        val key: String = CacheKeyPrefix.userFollowerCount(userId)
        initFollowerCountCacheIfAbsent(userId = userId, key = key)
        return cacheProvider.get(key)?.toLong() ?: 0L
    }

    // 팔로잉 수
    @Transactional(readOnly = true)
    fun fetchFollowingCount(userId: Long): Long {
        val key: String = CacheKeyPrefix.userFollowingCount(userId)
        initFollowingCountCacheIfAbsent(userId = userId, key = key)
        return cacheProvider.get(key)?.toLong() ?: 0L
    }

    // userId를 팔로우하는 회원 목록 (팔로워). nickname으로 부분 검색 가능.
    @Transactional(readOnly = true)
    fun searchUserFollowers(userId: Long, request: SearchUserFollowerRequest): PagingResponse<UserFollowResponse> {
        val queryFilter: UserFollowQueryFilter = userFollowConverter.convert(userId = userId, request = request)
        val paging = Paging(page = request.page, size = request.size)
        val userFollows: List<UserFollow> = userFollowRepository.searchUserFollows(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = request.orderTypes,
        )
        val totalCount: Long = userFollowRepository.searchUserFollowsCount(queryFilter)

        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = userFollowConverter.convertToFollowerResponses(userFollows),
            totalCount = totalCount
        )
    }

    // userId가 팔로우하는 회원 목록 (팔로잉). nickname으로 부분 검색 가능.
    @Transactional(readOnly = true)
    fun searchUserFollowings(userId: Long, request: SearchUserFollowingRequest): PagingResponse<UserFollowResponse> {
        val queryFilter: UserFollowQueryFilter = userFollowConverter.convert(userId = userId, request = request)
        val paging = Paging(page = request.page, size = request.size)
        val userFollows: List<UserFollow> = userFollowRepository.searchUserFollows(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = request.orderTypes,
        )
        val totalCount: Long = userFollowRepository.searchUserFollowsCount(queryFilter)

        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = userFollowConverter.convertToFollowingResponses(userFollows),
            totalCount = totalCount
        )
    }

    // 콜드스타트: Redis에 카운터 키가 없으면 DB COUNT로 시드값 세팅
    private fun initFollowerCountCacheIfAbsent(userId: Long, key: String) {
        if (!cacheProvider.hasKey(key)) {
            val count: Long = userFollowRepository.searchUserFollowsCount(
                UserFollowQueryFilter(followerUserId = null, followingUserId = userId, nickname = null)
            )
            cacheProvider.putIfAbsent(key, count.toString())
        }
    }

    private fun initFollowingCountCacheIfAbsent(userId: Long, key: String) {
        if (!cacheProvider.hasKey(key)) {
            val count: Long = userFollowRepository.searchUserFollowsCount(
                UserFollowQueryFilter(followerUserId = userId, followingUserId = null, nickname = null)
            )
            cacheProvider.putIfAbsent(key, count.toString())
        }
    }
}
