package com.chobolevel.domain.subject.image.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.subject.entity.Subject
import com.chobolevel.domain.subject.image.vo.SubjectImageType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "subjects_images")
@Audited
class SubjectImage private constructor(
    subject: Subject,
    type: SubjectImageType,
    path: String,
    name: String,
    sortOrder: Int,
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, updatable = false)
    val subject: Subject = subject

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    val type: SubjectImageType = type

    @Column(nullable = false, length = 255)
    val path: String = path

    @Column(nullable = false, length = 255)
    val name: String = name

    @Column(nullable = false)
    var sortOrder: Int = sortOrder
        protected set

    @Column(nullable = false)
    var isDeleted: Boolean = false
        protected set

    fun changeSortOrder(sortOrder: Int) {
        require(sortOrder > 0) { "정렬 순서는 반드시 0보다 커야 합니다." }
        this.sortOrder = sortOrder
    }

    fun delete() {
        this.isDeleted = true
    }

    companion object {
        internal fun create(subject: Subject, type: SubjectImageType, path: String, name: String, sortOrder: Int): SubjectImage {
            return SubjectImage(
                subject = subject,
                type = type,
                path = path,
                name = name,
                sortOrder = sortOrder,
            )
        }
    }
}
