package net.primal.domain.nostr.cryptography

interface MessageCipher {

    fun encryptMessage(
        userId: String,
        participantId: String,
        content: String,
    ): String

    fun decryptMessage(
        userId: String,
        participantId: String,
        content: String,
    ): String
}
