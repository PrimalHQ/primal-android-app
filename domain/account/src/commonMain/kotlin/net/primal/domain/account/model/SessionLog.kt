package net.primal.domain.account.model

data class SessionLog(
    val sessionId: String,
    val clientPubKey: String,
    val methodType: SignerMethodType,
    val eventKind: Int?,
    val isSuccess: Boolean,
    val methodPayload: String,
    val responsePayload: String,
)
