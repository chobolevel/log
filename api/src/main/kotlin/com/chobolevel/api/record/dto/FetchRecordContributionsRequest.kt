package com.chobolevel.api.record.dto

import java.time.LocalDate
import java.time.ZoneId

data class FetchRecordContributionsRequest(
    val userId: Long?,
    val year: Int = LocalDate.now(ZoneId.of("Asia/Seoul")).year,
)
