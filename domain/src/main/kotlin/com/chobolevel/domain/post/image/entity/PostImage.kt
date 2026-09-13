package com.chobolevel.domain.post.image.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.post.entity.Post
import com.chobolevel.domain.post.image.vo.PostImageType
import jakarta.persistence.Column
import jakarta.persistence.Entity
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

@Entity
@Table(name = "post_images")
@Audited
@SQLDelete(sql = "UPDATE post_images SET deleted = true WHERE id = ?")
class PostImage private constructor(
    type: PostImageType,
    name: String,
    path: String,
    width: Int,
    height: Int
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: PostImageType = type
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var path: String = path
        protected set

    @Column(nullable = false)
    var width: Int = width
        protected set

    @Column(nullable = false)
    var height: Int = height
        protected set

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: Post? = null
        protected set

    @Column(nullable = false)
    var deleted: Boolean = false
        protected set

    fun assignPost(post: Post) {
        if (this.post != post) {
            this.post = post
        }
    }

    fun delete() {
        this.deleted = true
    }

    companion object {
        fun create(type: PostImageType, name: String, path: String, width: Int, height: Int): PostImage = PostImage(
            type = type,
            name = name,
            path = path,
            width = width,
            height = height
        )
    }
}
