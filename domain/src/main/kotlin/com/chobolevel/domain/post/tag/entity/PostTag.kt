package com.chobolevel.domain.post.tag.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.tag.entity.Tag
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited

@Entity
@Table(name = "post_tags")
@Audited
class PostTag : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: Post? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    var tag: Tag? = null

    fun assignPost(post: Post) {
        if (this.post != post) {
            this.post = post
        }
    }

    fun assignTag(tag: Tag) {
        if (this.tag != tag) {
            this.tag = tag
        }
    }
}
