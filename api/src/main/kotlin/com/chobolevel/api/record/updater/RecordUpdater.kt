package com.chobolevel.api.record.updater

import com.chobolevel.api.record.dto.UpdateRecordRequest
import com.chobolevel.domain.emotion.entity.Emotion
import com.chobolevel.domain.emotion.repository.EmotionRepository
import com.chobolevel.domain.record.entity.Record
import com.chobolevel.domain.record.vo.RecordUpdateMask
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.repository.SubjectRepository
import org.springframework.stereotype.Component

@Component
class RecordUpdater(
    private val subjectRepository: SubjectRepository,
    private val emotionRepository: EmotionRepository
) {

    fun markAsUpdate(request: UpdateRecordRequest, entity: Record): Record {
        request.updateMask.forEach {
            when (it) {
                RecordUpdateMask.TYPE -> {
                    val reviewSubject: Subject? = request.review?.subjectId?.let { subjectId -> subjectRepository.findById(subjectId) }
                    val emotion: Emotion? = request.emotion?.emotionId?.let { emotionId -> emotionRepository.findById(emotionId) }
                    entity.changeType(
                        type = request.type!!,
                        reviewSubject = reviewSubject,
                        reviewRating = request.review?.rating,
                        emotion = emotion,
                        emotionIntensity = request.emotion?.intensity
                    )
                }
                RecordUpdateMask.TITLE -> entity.changeTitle(request.title!!)
                RecordUpdateMask.CONTENT -> entity.changeContent(request.content!!)
                RecordUpdateMask.IS_PRIVATE -> entity.changePrivacy(request.isPrivate!!)
                RecordUpdateMask.TAGS -> entity.replaceTags(request.tags!!)
            }
        }
        return entity
    }
}
