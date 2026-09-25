package com.chobolevel.api.record.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.common.extension.toKST
import com.chobolevel.api.record.converter.RecordConverter
import com.chobolevel.api.record.dto.CreateRecordRequest
import com.chobolevel.api.record.dto.RecordContributionResponse
import com.chobolevel.api.record.dto.RecordDetailResponse
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.dto.SearchRecordRequest
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.api.record.like.service.RecordLikeQueryService
import com.chobolevel.api.record.updater.RecordUpdater
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.api.record.view.service.RecordViewQueryService
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.repository.EmotionRepository
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.vo.RecordQueryFilter
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.repository.SubjectRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
class RecordService(
    private val recordRepository: RecordRepository,
    private val userRepository: UserRepository,
    private val subjectRepository: SubjectRepository,
    private val emotionRepository: EmotionRepository,
    private val recordConverter: RecordConverter,
    private val recordBusinessValidator: RecordBusinessValidator,
    private val recordLikeQueryService: RecordLikeQueryService,
    private val recordViewQueryService: RecordViewQueryService,
    private val recordUpdater: RecordUpdater
) {

    @Transactional
    fun createRecord(userId: Long, request: CreateRecordRequest): Long {
        val user: User = userRepository.findById(userId)
        val reviewSubject: Subject? = request.review?.subjectId?.let { subjectRepository.findById(it) }
        val emotion: Emotion? = request.emotion?.emotionId?.let { emotionRepository.findById(it) }
        val record: Record = Record.create(
            user = user,
            type = request.type,
            title = request.title,
            content = request.content,
            isPrivate = request.isPrivate,
            reviewSubject = reviewSubject,
            reviewRating = request.review?.rating,
            emotion = emotion,
            emotionIntensity = request.emotion?.intensity
        )
        record.replaceTags(request.tags)
        return recordRepository.save(record).id!!
    }

    @Transactional(readOnly = true)
    fun searchRecords(request: SearchRecordRequest): PagingResponse<RecordResponse> {
        val queryFilter: RecordQueryFilter = recordConverter.convert(request)
        val paging: Paging = Paging(page = request.page, size = request.size)
        val records: List<Record> = recordRepository.searchRecords(
            queryFilter = queryFilter,
            paging = paging,
            orderTypes = request.orderTypes
        )
        val totalCount: Long = recordRepository.searchRecordsCount(queryFilter)
        val recordIds: List<Long> = records.map { it.id!! }
        val likeCounts: Map<Long, Long> = recordLikeQueryService.fetchLikeCounts(recordIds)
        val viewCounts: Map<Long, Long> = recordViewQueryService.fetchViewCounts(recordIds)
        val responses: List<RecordResponse> = recordConverter.convert(records, likeCounts, viewCounts)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = responses,
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchRecord(requesterId: Long?, recordId: Long): RecordDetailResponse {
        val record: Record = recordRepository.findById(recordId)
        recordBusinessValidator.validateReadable(requesterId = requesterId, record = record)
        val likeCount: Long = recordLikeQueryService.fetchLikeCount(recordId)
        val viewCount: Long = recordViewQueryService.fetchViewCount(recordId)
        return recordConverter.convertToDetail(record, likeCount, viewCount)
    }

    @Transactional
    fun updateRecord(userId: Long, recordId: Long, request: UpdateRecordRequest): Long {
        val record: Record = recordRepository.findById(recordId)
        recordBusinessValidator.validateWriter(userId, record)
        recordUpdater.markAsUpdate(request, record)
        return record.id!!
    }

    @Transactional
    fun deleteRecord(userId: Long, recordId: Long): Boolean {
        val record: Record = recordRepository.findById(recordId)
        recordBusinessValidator.validateWriter(userId, record)
        record.delete()
        return true
    }

    // 연도별 잔디 — 비공개 기록도 카운트에 포함하되(공개 프로필에서도 활동량만 노출), 소프트 삭제된 기록은 제외한다.
    // 날짜 경계는 저장 타임존 설정과 무관하게 항상 KST 기준으로 계산한다.
    // year 기본값(현재 연도) 보정은 컨트롤러 경계(FetchRecordContributionsRequest)에서 이미 끝나 있으므로 여기서는 다루지 않는다.
    @Transactional(readOnly = true)
    fun fetchContributions(userId: Long, year: Int): List<RecordContributionResponse> {
        val zoneId: ZoneId = ZoneId.of("Asia/Seoul")
        val start: OffsetDateTime = LocalDate.of(year, 1, 1).atStartOfDay(zoneId).toOffsetDateTime()
        val end: OffsetDateTime = LocalDate.of(year + 1, 1, 1).atStartOfDay(zoneId).toOffsetDateTime()

        val createdAts: List<OffsetDateTime> = recordRepository.findCreatedAtsByUserIdAndCreatedAtBetween(
            userId = userId,
            start = start,
            end = end
        )
        val countsByDate: Map<LocalDate, Long> = createdAts.groupingBy { it.toKST() }.eachCount()
            .mapValues { it.value.toLong() }

        return recordConverter.convertToContributions(year = year, countsByDate = countsByDate)
    }
}
