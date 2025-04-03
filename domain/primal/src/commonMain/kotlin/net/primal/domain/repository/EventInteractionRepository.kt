package net.primal.domain.repository

import kotlinx.serialization.json.JsonArray
import net.primal.domain.publisher.PrimalPublishResult

interface EventInteractionRepository {

    suspend fun likeEvent(
        userId: String,
        eventId: String,
        eventAuthorId: String,
        optionalTags: List<JsonArray> = emptyList(),
    ): PrimalPublishResult

    suspend fun repostEvent(
        userId: String,
        eventId: String,
        eventKind: Int,
        eventAuthorId: String,
        eventRawNostrEvent: String,
        optionalTags: List<JsonArray> = emptyList(),
    ): PrimalPublishResult
}
