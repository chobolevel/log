package com.chobolevel.api.record.service

import com.chobolevel.api.common.dto.PagingResponse
import com.chobolevel.api.record.converter.RecordConverter
import com.chobolevel.api.record.dto.CreateRecordRequest
import com.chobolevel.api.record.dto.RecordResponse
import com.chobolevel.api.record.dto.SearchRecordRequest
import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.api.record.like.service.RecordLikeService
import com.chobolevel.api.record.validator.RecordBusinessValidator
import com.chobolevel.domain.common.dto.Paging
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.common.exception.ForbiddenException
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.repository.RecordRepository
import com.chobolevel.domain.record.vo.RecordQueryFilter
import com.chobolevel.domain.record.vo.RecordUpdateMask
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.repository.SubjectRepository
import com.chobolevel.domain.user.entity.User
import com.chobolevel.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordService(
    private val recordRepository: RecordRepository,
    private val userRepository: UserRepository,
    private val subjectRepository: SubjectRepository,
    private val recordConverter: RecordConverter,
    private val recordBusinessValidator: RecordBusinessValidator,
    private val recordLikeService: RecordLikeService
) {

    @Transactional
    fun createRecord(userId: Long, request: CreateRecordRequest): Long {
        val user: User = userRepository.findById(userId)
        val reviewSubject: Subject? = request.review?.subjectId?.let { subjectRepository.findById(it) }
        val record: Record = Record.create(
            user = user,
            type = request.type,
            title = request.title,
            content = request.content,
            isPrivate = request.isPrivate,
            reviewSubject = reviewSubject,
            reviewRating = request.review?.rating
        )
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
        val likeCounts: Map<Long, Long> = recordLikeService.fetchLikeCounts(records.map { it.id!! })
        val responses: List<RecordResponse> = recordConverter.convert(records, likeCounts)
        return PagingResponse(
            page = paging.page,
            size = paging.size,
            data = responses,
            totalCount = totalCount
        )
    }

    @Transactional(readOnly = true)
    fun fetchRecord(requesterId: Long?, recordId: Long): RecordResponse {
        val record: Record = recordRepository.findById(recordId)
        if (record.isPrivate && record.user.id != requesterId) {
            throw ForbiddenException(errorCode = ErrorCode.PRIVATE_RECORD)
        }
        val likeCount: Long = recordLikeService.fetchLikeCount(recordId)
        return recordConverter.convert(record, likeCount)
    }

    @Transactional
    fun updateRecord(userId: Long, recordId: Long, request: UpdateRecordRequest): Long {
        val record: Record = recordRepository.findById(recordId)
        recordBusinessValidator.validateWriter(userId, record)
        request.updateMask.forEach { mask: RecordUpdateMask ->
            when (mask) {
                RecordUpdateMask.TYPE -> record.changeType(
                    type = request.type!!,
                    reviewSubject = request.review?.subjectId?.let { subjectRepository.findById(it) },
                    reviewRating = request.review?.rating
                )
                RecordUpdateMask.TITLE -> record.changeTitle(request.title!!)
                RecordUpdateMask.CONTENT -> record.changeContent(request.content!!)
                RecordUpdateMask.IS_PRIVATE -> record.changePrivacy(request.isPrivate!!)
            }
        }
        return record.id!!
    }

    @Transactional
    fun deleteRecord(userId: Long, recordId: Long): Boolean {
        val record: Record = recordRepository.findById(recordId)
        recordBusinessValidator.validateWriter(userId, record)
        record.delete()
        return true
    }
}
