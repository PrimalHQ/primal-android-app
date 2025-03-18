package net.primal.data.remote.api.upload.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UploadChunkContent(
    @SerialName("upload_id") val uploadId: String,
    @SerialName("file_length") val fileLength: Long,
    @SerialName("offset") val offset: Int,
    @SerialName("data") val base64Data: String,
)
