package net.primal.networking.primal.upload.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadChunkContent(
    @SerialName("upload_id") val uploadId: String,
    @SerialName("file_length") val fileLength: Long,
    @SerialName("offset") val offset: Int,
    @SerialName("data") val base64Data: String,
)
