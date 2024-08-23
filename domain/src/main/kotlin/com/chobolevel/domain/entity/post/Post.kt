package com.chobolevel.domain.entity.post

import com.chobolevel.domain.entity.Audit
import com.chobolevel.domain.entity.post.tag.PostTag
import com.chobolevel.domain.entity.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(value = [AuditingEntityListener::class])
@Entity
@Table(name = "posts")
@Audited
@SQLDelete(sql = "UPDATE posts SET deleted = true WHERE id = ?")
class Post(
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false)
    var subTitle: String,
    @Column(nullable = false)
    var content: String
) : Audit() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Column(nullable = false)
    var deleted: Boolean = false

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var postTags = mutableListOf<PostTag>()

    fun setBy(user: User) {
        if (this.user != user) {
            this.user = user
        }
    }

    fun addPostTag(postTag: PostTag) {
        if (!this.postTags.contains(postTag)) {
            this.postTags.add(postTag)
        }
    }
}

enum class PostOrderType {
    CREATED_AT_ASC,
    CREATED_AT_DESC,
    UPDATED_AT_ASC,
    UPDATED_AT_DESC
}

enum class PostUpdateMask {
    TAGS,
    TITLE,
    SUB_TITLE,
    CONTENT
}
