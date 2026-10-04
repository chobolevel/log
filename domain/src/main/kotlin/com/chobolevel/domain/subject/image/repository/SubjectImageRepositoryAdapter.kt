package com.chobolevel.domain.subject.image.repository

import com.chobolevel.domain.common.exception.BusinessException
import com.chobolevel.domain.subject.exception.SubjectErrorCode
import com.chobolevel.domain.subject.image.entity.SubjectImage
import org.springframework.stereotype.Component

@Component
class SubjectImageRepositoryAdapter(
    private val subjectImageJpaRepository: SubjectImageJpaRepository
) : SubjectImageRepository {

    override fun save(subjectImage: SubjectImage): SubjectImage {
        return subjectImageJpaRepository.save(subjectImage)
    }

    override fun findById(id: Long): SubjectImage {
        return subjectImageJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw BusinessException(
            errorCode = SubjectErrorCode.SUBJECT_IMAGE_NOT_FOUND
        )
    }
}
