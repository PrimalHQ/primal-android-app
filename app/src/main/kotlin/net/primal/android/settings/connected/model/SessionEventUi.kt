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

private object NostrKinds {
    const val METADATA = 0
    const val TEXT_NOTE = 1
    const val CONTACTS = 3
    const val REPOST = 6
    const val REACTION = 7
    const val RELAY_LIST = 10002
}

private fun String.toTitle(): String {
    return when {
        this == "get_public_key" -> "Read Public Key"
        this.startsWith("sign_event:") -> toSignEventTitle()
        this == "nip04_encrypt" || this == "nip44_encrypt" -> "Encrypt"
        this == "nip04_decrypt" || this == "nip44_decrypt" -> "Decrypt"
        else -> this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

private fun String.toSignEventTitle(): String {
    val kind = this.substringAfter(':').toIntOrNull()
    return when (kind) {
        NostrKinds.METADATA -> "Update Profile"
        NostrKinds.TEXT_NOTE -> "Publish Note"
        NostrKinds.CONTACTS -> "Update Follow List"
        NostrKinds.REPOST -> "Repost"
        NostrKinds.REACTION -> "React to Note"
        NostrKinds.RELAY_LIST -> "Update Relay List"
        else -> "Sign Event (Kind $kind)"
    }
}
