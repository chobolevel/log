package com.chobolevel.domain.entity.post.comment

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.post.Post
import com.chobolevel.domain.entity.user.User
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
class PostComment(
    @Column(nullable = false)
    var content: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: Post? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    var writer: User? = null

    @Column(nullable = false)
    var deleted: Boolean = false

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

    fun delete() {
        this.deleted = true
    }
}

enum class PostCommentOrderType {
    CREATED_AT_ASC,
    CREATED_AT_DESC,
}

enum class PostCommentUpdateMask {
    CONTENT,
}
