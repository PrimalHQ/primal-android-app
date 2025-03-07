package net.primal.networking.primal.upload.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadCompleteContent(
    @SerialName("upload_id") val uploadId: String,
    @SerialName("file_length") val fileLength: Long,
    @SerialName("sha256") val hash: String,
)
