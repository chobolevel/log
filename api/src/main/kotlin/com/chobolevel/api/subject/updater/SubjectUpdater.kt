package com.chobolevel.api.subject.updater

import com.chobolevel.api.subject.dto.UpdateSubjectRequest
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.vo.SubjectUpdateMask
import org.springframework.stereotype.Component

@Component
class SubjectUpdater : SubjectUpdatable {

    override fun markAsUpdate(request: UpdateSubjectRequest, entity: Subject): Subject {
        request.updateMask.forEach {
            when (it) {
                SubjectUpdateMask.TYPE -> entity.type = request.type!!
                SubjectUpdateMask.TITLE -> entity.title = request.title!!
                SubjectUpdateMask.DESCRIPTION -> entity.description = request.description
            }
        }
        return entity
    }

    override fun order(): Int {
        return 0
    }
}
