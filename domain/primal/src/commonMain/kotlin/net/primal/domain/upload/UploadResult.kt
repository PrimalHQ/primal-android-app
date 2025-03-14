package net.primal.domain.upload

data class UploadResult(
    val remoteUrl: String,
    val originalFileSize: Int,
    val originalHash: String,
)
