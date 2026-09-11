package com.chobolevel.api.upload.dto


data class UploadResponse(
    val presignedUrl: String,
    val host: String,
    val path: String,
    val filenameWithExtension: String
)
