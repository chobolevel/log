package com.chobolevel.domain.subject.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.common.exception.DataNotFoundException
import com.chobolevel.domain.common.exception.ErrorCode
import com.chobolevel.domain.subject.dto.CreateSubjectCommand
import com.chobolevel.domain.subject.dto.SyncSubjectImageCommand
import com.chobolevel.domain.subject.dto.UpdateSubjectCommand
import com.chobolevel.domain.subject.image.entity.SubjectImage
import com.chobolevel.domain.subject.vo.SubjectType
import com.chobolevel.domain.subject.vo.SubjectUpdateMask
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited

@SQLDelete(sql = "UPDATE subjects SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
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
        protected set

    @Where(clause = "is_deleted = false")
    @OneToMany(mappedBy = "subject", cascade = [(CascadeType.ALL)], orphanRemoval = true)
    private val _images: MutableList<SubjectImage> = mutableListOf()

    val images: List<SubjectImage> get() = _images.toList()

    fun changeType(type: SubjectType) {
        this.type = type
    }

    fun changeTitle(title: String) {
        this.title = title
    }

    fun changeDescription(description: String?) {
        this.description = description
    }

    fun update(command: UpdateSubjectCommand) {
        command.updateMask.forEach { mask ->
            when (mask) {
                SubjectUpdateMask.TYPE -> changeType(type = command.type!!)
                SubjectUpdateMask.TITLE -> changeTitle(title = command.title!!)
                SubjectUpdateMask.DESCRIPTION -> changeDescription(description = command.description)
                SubjectUpdateMask.IMAGES -> syncImages(commands = command.images)
            }
        }
    }

    private fun syncImages(commands: List<SyncSubjectImageCommand>) {
        require(commands.size <= 6) { "이미지는 최대 6개까지 설정 가능합니다." }
        require(commands.all { it.sortOrder > 0 }) { "이미지 정렬 순서는 0보다 커야 합니다." }
        val sortOrders: List<Int> = commands.map { it.sortOrder }
        require(sortOrders.size == sortOrders.toSet().size) { "중복된 정렬 순서가 있습니다." }

        val requestIds: Set<Long> = commands.mapNotNull { it.id }.toSet()
        _images.retainAll { image -> image.id in requestIds }

        commands.forEach { command ->
            if (command.id == null) {
                _images.add(
                    SubjectImage.create(
                        subject = this,
                        path = command.path,
                        name = command.name,
                        sortOrder = command.sortOrder
                    )
                )
            } else {
                val image: SubjectImage = _images.find { it.id == command.id }
                    ?: throw DataNotFoundException(errorCode = ErrorCode.SUBJECT_IMAGE_NOT_FOUND)
                image.sync(path = command.path, name = command.name, sortOrder = command.sortOrder)
            }
        }
    }

    companion object {
        fun create(command: CreateSubjectCommand): Subject {
            val subject: Subject = Subject(
                type = command.type,
                title = command.title,
                description = command.description
            )
            subject.syncImages(commands = command.images)
            return subject
        }
    }
}
