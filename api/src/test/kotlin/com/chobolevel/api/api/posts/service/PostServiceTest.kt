package com.chobolevel.api.api.posts.service

import com.chobolevel.api.service.post.PostService
import com.chobolevel.api.service.post.converter.PostConverter
import com.chobolevel.domain.entity.post.PostRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock

@DisplayName("게시글 서비스 단위 테스트")
class PostServiceTest {

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var postConverter: PostConverter

    @InjectMocks
    private lateinit var postService: PostService

    @Test
    fun `게시글 등록`() {
        // given

        // when

        // then
    }
}
