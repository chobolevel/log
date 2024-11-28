package com.chobolevel.api.controller.v1.upload

import com.chobolevel.api.annotation.HasAuthorityAny
import com.chobolevel.api.annotation.HasAuthorityUser
import com.chobolevel.api.dto.common.ResultResponse
import com.chobolevel.api.dto.upload.UploadRequestDto
import com.chobolevel.api.service.upload.UploadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Upload (파일 업로드)", description = "파일 업로드 API")
@RestController
@RequestMapping("/api/v1")
class UploadController(
    private val service: UploadService
) {

    @Operation(summary = "파일 업로드용 경로 발급 API")
    @HasAuthorityAny
    @PostMapping("/upload/presigned-url")
    fun issuePresignedUrl(@RequestBody request: UploadRequestDto): ResponseEntity<ResultResponse> {
        val result = service.getPresignedUrl(request)
        return ResponseEntity.ok(ResultResponse(result))
    }
}
