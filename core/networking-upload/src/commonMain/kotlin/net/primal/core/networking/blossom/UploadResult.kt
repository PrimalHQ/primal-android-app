package net.primal.core.networking.blossom

sealed class UploadResult {
    data class Success(
        val remoteUrl: String,
        val originalFileSize: Long,
        val originalHash: String,
        val nip94: Nip94Metadata? = null,
    ) : UploadResult()

    data class Failed(
        val error: BlossomException,
        val message: String? = null,
    ) : UploadResult()
}
