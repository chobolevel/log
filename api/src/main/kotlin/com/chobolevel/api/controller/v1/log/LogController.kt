package com.chobolevel.api.controller.v1.log

import com.chobolevel.api.annotation.HasAuthorityAdmin
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.domain.exception.ApiException
import com.chobolevel.domain.exception.ErrorCode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

@Tag(name = "Log (로그)", description = "로그 관리 API")
@RestController
@RequestMapping("/api/v1/logs")
class LogController(
    @Value("\${nginx.log-path}")
    private val nginxLogPath: String
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Operation(summary = "nginx log 조회 API")
    @HasAuthorityAdmin
    @GetMapping("/nginx")
    fun getNginxAccessLogs(@RequestParam(required = true) filename: String): ResponseEntity<ResultResponse> {
        try {
            val reader = BufferedReader(FileReader("$nginxLogPath/$filename"))
            return ResponseEntity.ok(ResultResponse(reader.lines().toList()))
        } catch (e: IOException) {
            logger.error("error occurred while reading file", e)
            throw ApiException(
                errorCode = ErrorCode.L001,
                message = "로그 파일을 찾을 수 없습니다."
            )
        }
    }
}
