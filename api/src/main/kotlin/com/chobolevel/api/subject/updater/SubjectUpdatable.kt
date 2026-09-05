package com.chobolevel.api.subject.updater

import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.domain.subject.entity.Subject

interface SubjectUpdatable {

    fun markAsUpdate(request: UpdateSubjectRequest, entity: Subject): Subject

    fun order(): Int
}
