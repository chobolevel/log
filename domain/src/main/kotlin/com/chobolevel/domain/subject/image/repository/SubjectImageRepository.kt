package com.chobolevel.domain.subject.image.repository

import com.chobolevel.domain.subject.image.entity.SubjectImage

interface SubjectImageRepository {

    fun save(subjectImage: SubjectImage): SubjectImage

    fun findById(id: Long): SubjectImage
}
