package net.primal.domain.polls

import kotlinx.serialization.json.JsonArray

data class PreparedZapPollVote(
    val recipientUserId: String,
    val recipientLnUrl: String,
    val pollOptionTag: JsonArray,
)
