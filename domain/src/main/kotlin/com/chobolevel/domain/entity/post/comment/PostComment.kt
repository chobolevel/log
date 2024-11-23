package com.chobolevel.domain.entity.post.comment

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.post.Post
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "post_comments")
@Audited
@SQLDelete(sql = "UPDATE post_comments SET deleted = true WHERE id = ?")
class PostComment(
    @Column(nullable = false)
    var writerName: String,
    @Column(nullable = false)
    var password: String,
    @Column(nullable = false)
    var content: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: Post? = null

    @Column(nullable = false)
    var deleted: Boolean = false

    fun setBy(post: Post) {
        if (this.post != post) {
            this.post = post
        }
    }
}

enum class PostCommentOrderType {
    CREATED_AT_ASC,
    CREATED_AT_DESC,
}

enum class PostCommentUpdateMask {
    CONTENT,
    DELETE
}
