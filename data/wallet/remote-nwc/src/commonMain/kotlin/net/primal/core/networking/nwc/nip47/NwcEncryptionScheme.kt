package net.primal.core.networking.nwc.nip47

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NwcEncryptionScheme(val value: String) {
    @SerialName("nip04")
    NIP04("nip04"),

    @SerialName("nip44_v2")
    NIP44("nip44_v2"),
    ;

    companion object {
        fun fromValue(value: String): NwcEncryptionScheme? = entries.find { it.value == value }

        fun fromValueOrDefault(value: String?): NwcEncryptionScheme = value?.let { fromValue(it) } ?: NIP04
    }
}
