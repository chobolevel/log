package com.chobolevel.domain.tag.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.post.tag.entity.PostTag
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "tags")
@Audited
class Tag private constructor(
    name: String,
    order: Int
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var order: Int = order
        protected set

    @Column(nullable = false)
    var deleted: Boolean = false
        protected set

    @OneToMany(mappedBy = "tag", cascade = [CascadeType.ALL], orphanRemoval = true)
    var postTags = mutableListOf<PostTag>()

    fun updateName(name: String) {
        this.name = name
    }

    fun updateOrder(order: Int) {
        this.order = order
    }

    fun addPostTag(postTag: PostTag) {
        if (!this.postTags.contains(postTag)) {
            this.postTags.add(postTag)
        }
    }

    fun delete() {
        this.deleted = true
    }

    companion object {
        fun create(name: String, order: Int): Tag = Tag(name = name, order = order)
    }
}
