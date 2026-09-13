package com.chobolevel.domain.post.comment.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.user.entity.User
import jakarta.persistence.Column
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
@Table(name = "post_comments")
@Audited
class PostComment private constructor(
    content: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var content: String = content
        protected set

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: Post? = null
        protected set

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    var writer: User? = null
        protected set

    @Column(nullable = false)
    var deleted: Boolean = false
        protected set

    fun setBy(post: Post) {
        if (this.post != post) {
            this.post = post
        }
    }

    fun setBy(user: User) {
        if (this.writer != user) {
            this.writer = user
        }
    }

    fun updateContent(content: String) {
        this.content = content
    }

    fun delete() {
        this.deleted = true
    }

    companion object {
        fun create(content: String): PostComment = PostComment(content = content)
    }
}
