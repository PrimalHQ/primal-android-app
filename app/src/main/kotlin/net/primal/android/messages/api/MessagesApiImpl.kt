package net.primal.android.messages.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.messages.api.model.ConversationRequestBody
import net.primal.android.messages.api.model.ConversationsResponse
import net.primal.android.messages.api.model.MarkMessagesReadRequestBody
import net.primal.android.messages.api.model.MessagesRequestBody
import net.primal.android.messages.api.model.MessagesResponse
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.nostr.ext.asMessageConversationsSummary
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary

class MessagesApiImpl @Inject constructor(
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : MessagesApi {

    override suspend fun getConversations(userId: String, relation: ConversationRelation): ConversationsResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_DM_CONTACTS,
                optionsJson = NostrJson.encodeToString(
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
        )
    }

    override suspend fun getMessages(body: MessagesRequestBody): MessagesResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.GET_DMS,
                optionsJson = NostrJson.encodeToString(body),
            ),
        )

        return MessagesResponse(
            paging = response.findPrimalEvent(NostrEventKind.PrimalPaging)?.let {
                NostrJson.decodeFromStringOrNull(it.content)
            },
            messages = response.filterNostrEvents(NostrEventKind.EncryptedDirectMessages),
            profileMetadata = response.filterNostrEvents(NostrEventKind.Metadata),
            cdnResources = response.filterPrimalEvents(NostrEventKind.PrimalCdnResource),
        )
    }

    override suspend fun markConversationAsRead(userId: String, conversationUserId: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.MARK_DM_CONVERSATION_AS_READ,
                optionsJson = NostrJson.encodeToString(
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
                primalVerb = PrimalVerb.MARK_ALL_DMS_AS_READ,
                optionsJson = NostrJson.encodeToString(
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
