package net.primal.android.networking.primal.upload.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadCancelContent(
    @SerialName("upload_id") val uploadId: String,
)
