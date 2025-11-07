package net.primal.domain.account.model

data class SignerLog(
    val sessionId: String,
    val clientPubKey: String,
    val methodType: String,
    val eventKind: Int?,
    val isSuccess: Boolean,
    val methodPayload: String,
    val responsePayload: String,
)
