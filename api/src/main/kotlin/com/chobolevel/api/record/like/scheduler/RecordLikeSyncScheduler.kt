package com.chobolevel.api.record.like.scheduler

import com.chobolevel.api.common.constant.CacheKeyPrefix
import com.chobolevel.api.common.provider.CacheProvider
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.like.entity.RecordLike
import com.chobolevel.domain.record.like.repository.RecordLikeRepository
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RecordLikeSyncScheduler(
    private val cacheProvider: CacheProvider,
    private val recordRepository: RecordRepository,
    private val userRepository: UserRepository,
    private val recordLikeRepository: RecordLikeRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    fun sync() {
        val dirtyRecordIds: Set<String> = cacheProvider.getSetMembers(CacheKeyPrefix.RECORD_LIKES_DIRTY)
        if (dirtyRecordIds.isEmpty()) return

        logger.info("RecordLike sync 시작 - dirty records: ${dirtyRecordIds.size}개")

        dirtyRecordIds.forEach { recordIdStr ->
            val recordId: Long = recordIdStr.toLong()
            val likesKey: String = CacheKeyPrefix.recordLikes(recordId)

            val cachedUserIds: Set<Long> = cacheProvider.getSetMembers(likesKey).map { it.toLong() }.toSet()
            val savedUserIds: Set<Long> = recordLikeRepository
                .findAllByRecordId(recordId)
                .map { it.user.id!! }
                .toSet()

            val toAdd: Set<Long> = cachedUserIds - savedUserIds
            val toRemove: Set<Long> = savedUserIds - cachedUserIds

            if (toAdd.isNotEmpty()) {
                val record: Record = recordRepository.findById(recordId)
                toAdd.forEach { userId ->
                    val user: User = userRepository.findById(userId)
                    recordLikeRepository.save(RecordLike.create(record = record, user = user))
                }
            }

            toRemove.forEach { userId ->
                recordLikeRepository.deleteByRecordIdAndUserId(recordId = recordId, userId = userId)
            }

            cacheProvider.removeFromSet(CacheKeyPrefix.RECORD_LIKES_DIRTY, recordIdStr)
        }

        logger.info("RecordLike sync 완료")
    }
}
