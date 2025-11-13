package net.primal.android.settings.connected.model

import net.primal.domain.account.model.SessionEvent

data class SessionEventUi(
    val id: String,
    val title: String,
    val timestamp: Long,
)

fun SessionEvent.asSessionEventUi(): SessionEventUi {
    return SessionEventUi(
        id = this.eventId,
        title = this.requestTypeId.toTitle(),
        timestamp = this.requestedAt,
    )
}

private fun String.toTitle(): String {
    return when {
        this == "get_public_key" -> "Read Public Key"
        this.startsWith("sign_event:") -> {
            val kind = this.substringAfter(':').toIntOrNull()
            when (kind) {
                0 -> "Update Profile"
                1 -> "Publish Note"
                3 -> "Update Follow List"
                6 -> "Repost"
                7 -> "React to Note"
                10002 -> "Update Relay List"
                else -> "Sign Event (Kind $kind)"
            }
        }
        this == "nip04_encrypt" || this == "nip44_encrypt" -> "Encrypt"
        this == "nip04_decrypt" || this == "nip44_decrypt" -> "Decrypt"
        else -> this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
