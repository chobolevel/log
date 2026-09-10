package com.chobolevel.domain.subject.image.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.subject.entity.Subject
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited

@SQLDelete(sql = "UPDATE subjects_images SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Entity
@Table(name = "subjects_images")
@Audited
class SubjectImage private constructor(
    subject: Subject,
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

    @Column(nullable = false, length = 255)
    var path: String = path
        protected set

    @Column(nullable = false, length = 255)
    var name: String = name
        protected set

    @Column(nullable = false)
    var sortOrder: Int = sortOrder
        protected set

    @Column(nullable = false)
    var isDeleted: Boolean = false
        protected set

    internal fun sync(path: String, name: String, sortOrder: Int) {
        this.path = path
        this.name = name
        this.sortOrder = sortOrder
    }

    companion object {
        internal fun create(subject: Subject, path: String, name: String, sortOrder: Int): SubjectImage {
            return SubjectImage(
                subject = subject,
                path = path,
                name = name,
                sortOrder = sortOrder,
            )
        }
    }
}
