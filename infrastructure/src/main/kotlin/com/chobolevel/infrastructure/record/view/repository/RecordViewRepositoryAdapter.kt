package com.chobolevel.infrastructure.record.view.repository

import com.chobolevel.domain.record.view.entity.RecordView
import com.chobolevel.domain.record.view.repository.RecordViewRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class RecordViewRepositoryAdapter(
    private val recordViewJpaRepository: RecordViewJpaRepository,
) : RecordViewRepository {

    override fun save(recordView: RecordView): RecordView {
        return recordViewJpaRepository.save(recordView)
    }

    override fun countByRecordId(recordId: Long): Long {
        return recordViewJpaRepository.countByRecordId(recordId = recordId)
    }

    override fun findRecentRecordIdsByUserId(userId: Long, limit: Int): List<Long> {
        return recordViewJpaRepository
            .findRecordIdsByUserIdOrderByIdDesc(userId = userId, pageable = PageRequest.of(0, limit * RECENT_READ_FACTOR))
            .distinct()
            .take(limit)
    }

    override fun findRecentRecordIdsByGuestId(guestId: String, limit: Int): List<Long> {
        return recordViewJpaRepository
            .findRecordIdsByGuestIdOrderByIdDesc(guestId = guestId, pageable = PageRequest.of(0, limit * RECENT_READ_FACTOR))
            .distinct()
            .take(limit)
    }

    companion object {
        // 같은 기록을 여러 번 봤어도 limit개를 채울 여유를 두되 이력 전체를 읽지는 않도록 최근 이력을 limit의 이 배수만큼만 읽는다
        private const val RECENT_READ_FACTOR = 2
    }
}
