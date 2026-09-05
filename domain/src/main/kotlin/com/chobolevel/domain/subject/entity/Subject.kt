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
class Subject(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: SubjectType,
    @Column(nullable = false)
    var title: String,
    @Column(columnDefinition = "text")
    var description: String?,
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var isDeleted: Boolean = false

    fun delete() {
        this.isDeleted = true
    }
}
