package com.chobolevel.domain.post.entity

import com.chobolevel.domain.common.entity.Audit
import com.chobolevel.domain.post.image.entity.PostImage
import com.chobolevel.domain.post.image.vo.PostImageType
import com.chobolevel.domain.post.tag.entity.PostTag
import com.chobolevel.domain.tag.entity.Tag
import com.chobolevel.domain.user.entity.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Where
import org.hibernate.envers.Audited

@Entity
@Table(name = "posts")
@Audited
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
    var postTags = mutableSetOf<PostTag>()

    @Where(clause = "deleted = false")
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var postImages = mutableSetOf<PostImage>()

    fun assignWriter(user: User) {
        if (this.user != user) {
            this.user = user
        }
    }

    fun addPostImage(postImage: PostImage) {
        if (!this.postImages.contains(postImage)) {
            this.postImages.add(postImage)
            postImage.assignPost(this)
        }
    }

    fun removePostImage(postImage: PostImage) {
        if (this.postImages.contains(postImage)) {
            this.postImages.remove(postImage)
        }
    }

    fun getThumbnailImage(): PostImage? {
        return this.postImages.find { postImage -> postImage.type == PostImageType.THUMBNAIL }
    }

    fun addTags(tags: List<Tag>) {
        tags.forEach { tag ->
            val postTag = PostTag()
            postTag.assignTag(tag)
            postTag.assignPost(this)
            if (!this.postTags.contains(postTag)) {
                this.postTags.add(postTag)
            }
        }
    }

    fun delete() {
        this.deleted = true
    }
}
