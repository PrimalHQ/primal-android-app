package net.primal.core.networking.blossom

data class FileMetadata(
    val sha256: String,
    val sizeInBytes: Long,
    val mimeType: String? = null,
)
