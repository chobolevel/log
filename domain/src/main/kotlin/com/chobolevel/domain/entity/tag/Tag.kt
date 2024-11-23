package com.chobolevel.domain.entity.tag

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.post.tag.PostTag
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener

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

    @OneToMany(mappedBy = "tag", cascade = [CascadeType.ALL], orphanRemoval = true)
    var postTags = mutableListOf<PostTag>()

    fun addPostTag(postTag: PostTag) {
        if (!this.postTags.contains(postTag)) {
            this.postTags.add(postTag)
        }
    }
}

enum class TagOrderType {
    ORDER_ASC,
    ORDER_DESC
}

enum class TagUpdateMask {
    NAME,
    ORDER
}
