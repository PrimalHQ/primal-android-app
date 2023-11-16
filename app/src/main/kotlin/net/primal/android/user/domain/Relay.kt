package net.primal.android.user.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addAll
import kotlinx.serialization.json.buildJsonArray

@Serializable
data class Relay(
    val url: String,
    val read: Boolean,
    val write: Boolean,
)

fun String.toRelay(): Relay = Relay(url = this, read = true, write = true)

@OptIn(ExperimentalSerializationApi::class)
fun List<Relay>.toZapTag(): JsonArray =
    buildJsonArray {
        add("relays")
        addAll(this@toZapTag.map { it.url })
    }

@Serializable
data class RelayPermission(
    val read: Boolean,
    val write: Boolean,
)
