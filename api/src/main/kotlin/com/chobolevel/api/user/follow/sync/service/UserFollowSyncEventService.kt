package com.chobolevel.api.user.follow.sync.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.user.follow.sync.converter.UserFollowSyncEventConverter
import com.chobolevel.api.user.follow.sync.dto.SearchUserFollowSyncEventRequest
import com.chobolevel.api.user.follow.sync.dto.UserFollowSyncEventResponse
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.PolicyViolationException
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserFollowSyncEventService(
    private val userFollowSyncEventRepository: UserFollowSyncEventRepository,
    private val userFollowSyncEventConverter: UserFollowSyncEventConverter,
) {

    @Transactional(readOnly = true)
    fun searchFailedEvents(request: SearchUserFollowSyncEventRequest): PagingResponse<UserFollowSyncEventResponse> {
        val paging = Paging(page = request.page, size = request.size)
        val events: List<UserFollowSyncEvent> = userFollowSyncEventRepository.findAllByStatus(
            status = UserFollowSyncEventStatus.FAILED,
            paging = paging
        )
        val totalCount: Long = userFollowSyncEventRepository.countByStatus(UserFollowSyncEventStatus.FAILED)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = userFollowSyncEventConverter.convert(entities = events),
            totalCount = totalCount
        )
    }

    @Transactional
    fun retry(eventId: Long): Long {
        val event: UserFollowSyncEvent = userFollowSyncEventRepository.findById(eventId)
        if (event.status != UserFollowSyncEventStatus.FAILED) {
            throw PolicyViolationException(errorCode = ErrorCode.USER_FOLLOW_SYNC_EVENT_NOT_FAILED)
        }
        event.retry()
        return event.id!!
    }
}
