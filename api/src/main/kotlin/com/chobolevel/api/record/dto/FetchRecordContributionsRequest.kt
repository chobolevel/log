package com.chobolevel.api.record.dto

import com.chobolevel.api.common.time.nowKST
data class FetchRecordContributionsRequest(
    val userId: Long?,
    val year: Int = nowKST().year,
)
