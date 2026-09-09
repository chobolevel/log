package com.chobolevel.domain.subject.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.subject.vo.SubjectType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "subjects")
@Audited
class Subject private constructor(
    type: SubjectType,
    title: String,
    description: String?,
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: SubjectType = type
        protected set

    @Column(nullable = false)
    var title: String = title
        protected set

    @Column(columnDefinition = "text")
    var description: String? = description
        protected set

    @Column(nullable = false)
    var isDeleted: Boolean = false

    fun changeType(type: SubjectType) {
        this.type = type
    }

    fun changeTitle(title: String) {
        this.title = title
    }

    fun changeDescription(description: String?) {
        this.description = description
    }

    fun delete() {
        this.isDeleted = true
    }

    companion object {
        fun create(type: SubjectType, title: String, description: String?): Subject {
            return Subject(
                type = type,
                title = title,
                description = description
            )
        }
    }
}
