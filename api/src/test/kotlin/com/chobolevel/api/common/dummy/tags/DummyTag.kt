package com.chobolevel.api.common.dummy.tags

import com.chobolevel.domain.entity.tag.Tag

/**
 *
 * 태그 더미 클래스
 *
 * 테스트에 사용될 태그 더미 클래스입니다.
 *
 * 모든 객체는 싱글톤 패턴으로 관리되고 있습니다.
 *
 * @author chobolevel
 * @created 2025-02-04
 * @since 0.0.1
 */

object DummyTag {
    private val id = 1L
    private val name = "JAVA"
    private val order = 1

    fun toEntity(): Tag {
        return tag
    }

    private val tag: Tag by lazy {
        Tag(
            name = name,
            order = order
        ).also {
            it.id = id
        }
    }
}
