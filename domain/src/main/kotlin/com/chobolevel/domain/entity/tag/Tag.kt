package com.chobolevel.domain.entity.tag

import com.chobolevel.domain.entity.Audit
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(value = [AuditingEntityListener::class])
@Entity
@Table(name = "tags")
@Audited
@SQLDelete(sql = "UPDATE tags SET deleted = true WHERE id = ?")
class Tag(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var order: Int
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var deleted: Boolean = false
}

enum class TagOrderType {
    ORDER_ASC,
    ORDER_DESC
}

enum class TagUpdateMask {
    NAME,
    ORDER
}
