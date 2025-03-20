package net.primal.android.messages.api

import javax.inject.Inject
import net.primal.android.messages.api.model.ConversationRequestBody
import net.primal.android.messages.api.model.ConversationsResponse
import net.primal.android.messages.api.model.MarkMessagesReadRequestBody
import net.primal.android.messages.api.model.MessagesRequestBody
import net.primal.android.messages.api.model.MessagesResponse
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.ext.asMessageConversationsSummary
import net.primal.android.nostr.notary.NostrNotary
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.domain.nostr.NostrEventKind

class MessagesApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : MessagesApi {

    override suspend fun getConversations(userId: String, relation: ConversationRelation): ConversationsResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.GET_DM_CONTACTS.id,
                optionsJson = CommonJson.encodeToString(
                    ConversationRequestBody(
                        userId = userId,
                        relation = relation,
                    ),
                ),
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
                primalVerb = net.primal.data.remote.PrimalVerb.GET_DMS.id,
                optionsJson = CommonJson.encodeToString(body),
            ),
        )

        return MessagesResponse(
            paging = response.findPrimalEvent(NostrEventKind.PrimalPaging)?.let {
                CommonJson.decodeFromStringOrNull(it.content)
            },
            messages = response.filterNostrEvents(NostrEventKind.EncryptedDirectMessages),
            profileMetadata = response.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = response.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
            primalUserNames = response.findPrimalEvent(NostrEventKind.PrimalUserNames),
            primalLegendProfiles = response.findPrimalEvent(NostrEventKind.PrimalLegendProfiles),
            primalPremiumInfo = response.findPrimalEvent(NostrEventKind.PrimalPremiumInfo),
        )
    }

    override suspend fun markConversationAsRead(userId: String, conversationUserId: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MARK_DM_CONVERSATION_AS_READ.id,
                optionsJson = CommonJson.encodeToString(
                    MarkMessagesReadRequestBody(
                        authorization = nostrNotary.signAuthorizationNostrEvent(
                            userId = userId,
                            description = "Mark conversation with $conversationUserId as read.",
                        ),
                        conversationUserId = conversationUserId,
                    ),
                ),
            ),
        )
    }

    override suspend fun markAllMessagesAsRead(userId: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.MARK_ALL_DMS_AS_READ.id,
                optionsJson = CommonJson.encodeToString(
                    MarkMessagesReadRequestBody(
                        authorization = nostrNotary.signAuthorizationNostrEvent(
                            userId = userId,
                            description = "Mark all messages as read.",
                        ),
                    ),
                ),
            ),
        )
    }
}
