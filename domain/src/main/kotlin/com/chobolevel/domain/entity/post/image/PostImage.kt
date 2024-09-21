package com.chobolevel.domain.entity.post.image

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.post.Post
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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

@EntityListeners(value = [AuditingEntityListener::class])
@Entity
@Table(name = "post_images")
@Audited
@SQLDelete(sql = "UPDATE post_images SET deleted = true WHERE id = ?")
class PostImage(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: PostImageType,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var url: String,
    @Column(nullable = false)
    var width: Int,
    @Column(nullable = false)
    var height: Int
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
        post.addImage(this)
    }

    fun delete() {
        this.deleted = true
    }
}

enum class PostImageType {
    THUMB_NAIL
}

enum class PostImageUpdateMask {
    TYPE,
    NAME,
    URL,
    WIDTH,
    HEIGHT
}
