package net.primal.core.networking.blossom

sealed class UploadStatus {
    data object Uploading : UploadStatus()
    data object UploadCompleted : UploadStatus()
    data object UploadFailed : UploadStatus()
}
