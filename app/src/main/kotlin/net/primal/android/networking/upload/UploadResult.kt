package net.primal.android.networking.upload

data class UploadResult(
    val remoteUrl: String,
    val originalFileSize: Long,
    val originalHash: String,
)
