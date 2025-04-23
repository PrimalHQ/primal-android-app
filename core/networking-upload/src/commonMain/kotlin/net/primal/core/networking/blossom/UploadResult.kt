package net.primal.core.networking.blossom

sealed class UploadResult {
    data class Success(
        val remoteUrl: String,
        val originalFileSize: Long,
        val originalHash: String,
    ) : UploadResult()

    data class Failed(
        val error: BlossomException,
    ) : UploadResult()
}
