package net.primal.domain.upload

sealed class UploadStatus {
    data object Uploading : UploadStatus()
    data object UploadCompleted : UploadStatus()
    data object UploadFailed : UploadStatus()
}
