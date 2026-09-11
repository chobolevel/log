package com.chobolevel.api.upload.dto


data class UploadRequest(
    val prefix: String,
    val filename: String,
    val extension: String
)
