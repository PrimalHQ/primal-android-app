package net.primal.android.networking.primal.upload.domain

sealed class UploadStatus {
    data object Uploading : UploadStatus()
    data object UploadCompleted : UploadStatus()
    data object UploadFailed : UploadStatus()
}
