package com.chobolevel.api.record.like.scheduler

import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.like.entity.RecordLike
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class RecordLikeSyncProcessor(
    private val recordRepository: RecordRepository,
    private val userRepository: UserRepository,
    private val recordLikeRepository: RecordLikeRepository
) {

    // 독립 트랜잭션: 한 명 실패가 다른 처리에 영향 없음
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun addLike(recordId: Long, userId: Long) {
        val record: Record = recordRepository.findById(recordId)
        val user: User = userRepository.findById(userId)
        recordLikeRepository.save(RecordLike.create(record = record, user = user))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun removeLike(recordId: Long, userId: Long) {
        recordLikeRepository.deleteByRecordIdAndUserId(recordId = recordId, userId = userId)
    }
}
