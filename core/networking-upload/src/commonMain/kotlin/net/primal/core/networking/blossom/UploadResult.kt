package net.primal.core.networking.blossom

data class UploadResult(
    val remoteUrl: String,
    val originalFileSize: Long,
    val originalHash: String,
)
