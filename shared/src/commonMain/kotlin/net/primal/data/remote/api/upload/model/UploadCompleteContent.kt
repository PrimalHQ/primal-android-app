package net.primal.data.remote.api.upload.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UploadCompleteContent(
    @SerialName("upload_id") val uploadId: String,
    @SerialName("file_length") val fileLength: Long,
    @SerialName("sha256") val hash: String,
)
