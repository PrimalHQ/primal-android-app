package net.primal.android.networking.primal.upload.domain

data class UploadResult(
    val remoteUrl: String,
    val originalFileSize: Int,
    val originalHash: String,
)
