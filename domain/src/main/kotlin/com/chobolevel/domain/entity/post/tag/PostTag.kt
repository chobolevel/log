package com.chobolevel.domain.entity.post.tag

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.post.Post
import com.chobolevel.domain.entity.tag.Tag
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

    fun setBy(post: Post) {
        if (this.post != post) {
            this.post = post
        }
        post.addPostTag(this)
    }

    fun setBy(tag: Tag) {
        if (this.tag != tag) {
            this.tag = tag
        }
        tag.addPostTag(this)
    }
}
