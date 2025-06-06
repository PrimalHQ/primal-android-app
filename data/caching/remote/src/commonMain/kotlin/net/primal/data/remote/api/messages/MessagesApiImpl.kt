package net.primal.data.remote.api.messages

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.messages.model.ConversationRequestBody
import net.primal.data.remote.api.messages.model.ConversationsResponse
import net.primal.data.remote.api.messages.model.MarkMessagesReadRequestBody
import net.primal.data.remote.api.messages.model.MessagesRequestBody
import net.primal.data.remote.api.messages.model.MessagesResponse
import net.primal.data.remote.mapper.asMessageConversationsSummary
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

internal class MessagesApiImpl(
    private val primalApiClient: PrimalApiClient,
) : MessagesApi {

    override suspend fun getConversations(body: ConversationRequestBody): ConversationsResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_DM_CONTACTS.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return ConversationsResponse(
            conversationsSummary = response
                .findPrimalEvent(NostrEventKind.PrimalDirectMessagesConversationsSummary)
                ?.asMessageConversationsSummary(),
            messages = response.filterNostrEvents(NostrEventKind.EncryptedDirectMessages),
            profileMetadata = response.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = response.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = response.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = response.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = response.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
            blossomServers = response.filterNostrEvents(NostrEventKind.BlossomServerList),
        )
    }

    override suspend fun getMessages(body: MessagesRequestBody): MessagesResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_DMS.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )

        return MessagesResponse(
            paging = response.findPrimalEvent(NostrEventKind.PrimalPaging)?.content?.decodeFromJsonStringOrNull(),
            messages = response.filterNostrEvents(NostrEventKind.EncryptedDirectMessages),
            profileMetadata = response.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = response.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = response.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = response.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = response.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
        )
    }

    override suspend fun markConversationAsRead(body: MarkMessagesReadRequestBody) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MARK_DM_CONVERSATION_AS_READ.id,
                optionsJson = body.encodeToJsonString(),
            ),
        )
    }

    override suspend fun markAllMessagesAsRead(authorization: NostrEvent) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MARK_ALL_DMS_AS_READ.id,
                optionsJson = MarkMessagesReadRequestBody(authorization = authorization).encodeToJsonString(),
            ),
        )
    }
}
