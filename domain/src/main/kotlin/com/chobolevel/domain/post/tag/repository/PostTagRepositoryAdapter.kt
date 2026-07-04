package com.chobolevel.domain.post.tag.repository

import com.chobolevel.domain.post.tag.entity.PostTag
import org.springframework.stereotype.Component

@Component
class PostTagRepositoryAdapter(
    private val postTagJpaRepository: PostTagJpaRepository
) : PostTagRepository {

    override fun deleteAllInBatch(postTags: Collection<PostTag>) {
        postTagJpaRepository.deleteAllInBatch(postTags)
    }

    override fun deleteByPostId(postId: Long) {
        postTagJpaRepository.deleteByPostId(postId)
    }
}
