package net.primal.core.networking.blossom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlobDescriptor(
    val url: String,
    val sha256: String,
    @SerialName("size") val sizeInBytes: Long,
    val type: String? = null,
    val uploaded: Long,
    val nip94: List<List<String>>? = null,
)

fun List<List<String>>.toNip94Metadata(): Nip94Metadata {
    val map = this
        .mapNotNull { tag ->
            val key = tag.getOrNull(0)
            val value = tag.getOrNull(1)
            if (key != null && value != null) key to value else null
        }
        .toMap()

    return Nip94Metadata(
        url = map["url"],
        m = map["m"],
        x = map["x"],
        ox = map["ox"],
        size = map["size"]?.toLongOrNull(),
        dim = map["dim"],
        magnet = map["magnet"],
        i = map["i"],
        blurhash = map["blurhash"],
        thumb = map["thumb"],
        image = map["image"],
        summary = map["summary"],
        alt = map["alt"],
        fallback = map["fallback"],
        service = map["service"],
    )
}
