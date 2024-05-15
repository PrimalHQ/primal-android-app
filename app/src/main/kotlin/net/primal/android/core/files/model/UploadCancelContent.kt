package net.primal.android.core.files.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadCancelContent(
    @SerialName("upload_id") val uploadId: String,
)
