package com.chobolevel.domain.subject.image.repository

import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
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
        return subjectImageJpaRepository.findByIdAndIsDeletedFalse(id) ?: throw DataNotFoundException(
            errorCode = ErrorCode.SUBJECT_IMAGE_NOT_FOUND
        )
    }
}
