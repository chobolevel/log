package com.chobolevel.domain.record.like.repository

import com.chobolevel.domain.record.like.entity.RecordLike
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RecordLikeRepositoryAdapter(
    private val recordLikeJpaRepository: RecordLikeJpaRepository
) : RecordLikeRepository {

    override fun save(recordLike: RecordLike): RecordLike {
        return recordLikeJpaRepository.save(recordLike)
    }

    override fun findAllByRecordId(recordId: Long): List<RecordLike> {
        return recordLikeJpaRepository.findAllByRecordId(recordId = recordId)
    }

    @Transactional
    override fun deleteByRecordIdAndUserId(recordId: Long, userId: Long) {
        recordLikeJpaRepository.deleteByRecordIdAndUserId(
            recordId = recordId,
            userId = userId
        )
    }
}
