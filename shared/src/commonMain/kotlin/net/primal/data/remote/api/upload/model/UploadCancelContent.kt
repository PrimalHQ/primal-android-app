package net.primal.data.remote.api.upload.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UploadCancelContent(
    @SerialName("upload_id") val uploadId: String,
)
