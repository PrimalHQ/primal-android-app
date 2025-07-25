package net.primal.data.repository.messages.processors

import io.github.aakira.napier.Napier
import net.primal.data.local.dao.messages.DirectMessageData
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.api.users.UsersApi
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.feed.processors.persistToDatabaseAsTransaction
import net.primal.data.repository.mappers.remote.flatMapMessagesAsEventUriPO
import net.primal.data.repository.mappers.remote.flatMapMessagesAsReferencedNostrUriDO
import net.primal.data.repository.mappers.remote.mapAsMessageDataPO
import net.primal.data.repository.mappers.remote.mapAsPostDataPO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsPostDataPO
import net.primal.data.repository.mappers.remote.mapReferencedNostrUriAsEventUriNostrPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.domain.common.PrimalEvent
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.MessageCipher
import net.primal.domain.nostr.utils.extractNoteId
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.isNostrUri
import net.primal.shared.data.local.db.withTransaction

internal class MessagesProcessor(
    private val database: PrimalDatabase,
    private val feedApi: FeedApi,
    private val usersApi: UsersApi,
    private val messageCipher: MessageCipher,
) {

    suspend fun processMessageEventsAndSave(
        userId: String,
        messages: List<NostrEvent>,
        profileMetadata: List<NostrEvent>,
        mediaResources: List<PrimalEvent>,
        primalUserNames: PrimalEvent?,
        primalPremiumInfo: PrimalEvent?,
        primalLegendProfiles: PrimalEvent?,
        blossomServerEvents: List<NostrEvent>?,
    ) {
        val messageDataList = messages.mapAsMessageDataPO(
            userId = userId,
            onMessageDecrypt = messageCipher::decryptMessage,
        )

        processNostrUrisAndSave(userId = userId, messageDataList = messageDataList)

        val cdnResources = mediaResources.flatMapNotNullAsCdnResource()
        val primalUserNamesMap = primalUserNames.parseAndMapPrimalUserNames()
        val primalPremiumInfoMap = primalPremiumInfo.parseAndMapPrimalPremiumInfo()
        val primalLegendProfilesMap = primalLegendProfiles.parseAndMapPrimalLegendProfiles()
        val attachments = messageDataList.flatMapMessagesAsEventUriPO()
        val blossomServers = blossomServerEvents?.mapAsMapPubkeyToListOfBlossomServers() ?: emptyMap()

        database.withTransaction {
            database.profiles().insertOrUpdateAll(
                data = profileMetadata.mapAsProfileDataPO(
                    cdnResources = cdnResources,
                    primalUserNames = primalUserNamesMap,
                    primalPremiumInfo = primalPremiumInfoMap,
                    primalLegendProfiles = primalLegendProfilesMap,
                    blossomServers = blossomServers,
                ),
            )
            database.messages().upsertAll(data = messageDataList)
            database.eventUris().upsertAllEventUris(data = attachments)
        }
    }

    private suspend fun processNostrUrisAndSave(userId: String, messageDataList: List<DirectMessageData>) {
        val nostrUris = messageDataList.flatMap { it.uris }.filter { it.isNostrUri() }

        val referencedEventIds = nostrUris.mapNotNull { it.extractNoteId() }.toSet()
        val localNotes = database.posts().findPosts(referencedEventIds.toList())
        val missingEventIds = referencedEventIds - localNotes.map { it.postId }.toSet()
        val remoteNotes = if (missingEventIds.isNotEmpty()) {
            try {
                val response = feedApi.getNotes(noteIds = missingEventIds)
                response.persistToDatabaseAsTransaction(
                    userId = userId,
                    database = database,
                )
                val referencedPostsWithoutReplies = response.referencedEvents.mapNotNullAsPostDataPO()
                val referencedPostsWithReplies = response.referencedEvents.mapNotNullAsPostDataPO(
                    referencedPostsWithoutReplies,
                )
                response.notes.mapAsPostDataPO(
                    referencedPosts = referencedPostsWithReplies,
                    referencedArticles = emptyList(),
                    referencedHighlights = emptyList(),
                )
            } catch (error: NetworkException) {
                Napier.w(error) { "Failed to get notes for DMs." }
                emptyList()
            }
        } else {
            emptyList()
        }

        val allNotes = (localNotes + remoteNotes)
        val referencedNotesMap = allNotes.groupBy { it.postId }.mapValues { it.value.first() }

        val referencedProfileIds = nostrUris.mapNotNull { it.extractProfileId() }.toSet()
        val refNoteAuthorProfileIds = allNotes.map { it.authorId }.toSet()
        val allProfileIds = referencedProfileIds + refNoteAuthorProfileIds
        val localProfiles = database.profiles().findProfileData(allProfileIds.toList())
        val missingProfileIds = allProfileIds - localProfiles.map { it.ownerId }.toSet()
        val remoteProfiles = if (missingProfileIds.isNotEmpty()) {
            try {
                val response = usersApi.getUserProfilesMetadata(userIds = missingProfileIds)
                val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
                val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
                val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
                val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
                val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
                val profiles = response.metadataEvents.mapAsProfileDataPO(
                    cdnResources = cdnResources,
                    primalUserNames = primalUserNames,
                    primalPremiumInfo = primalPremiumInfo,
                    primalLegendProfiles = primalLegendProfiles,
                    blossomServers = blossomServers,
                )
                database.profiles().insertOrUpdateAll(data = profiles)
                profiles
            } catch (error: NetworkException) {
                Napier.w(error) { "Failed to get user profiles for DM messages." }
                emptyList()
            }
        } else {
            emptyList()
        }

        val referencedProfilesMap = (localProfiles + remoteProfiles)
            .groupBy { it.ownerId }
            .mapValues { it.value.first() }

        database.eventUris().upsertAllEventNostrUris(
            data = messageDataList.flatMapMessagesAsReferencedNostrUriDO(
                eventIdToNostrEvent = emptyMap(),
                postIdToPostDataMap = referencedNotesMap,
                articleIdToArticle = emptyMap(),
                profileIdToProfileDataMap = referencedProfilesMap,
                cdnResources = emptyMap(),
                linkPreviews = emptyMap(),
                videoThumbnails = emptyMap(),
            ).mapReferencedNostrUriAsEventUriNostrPO(),
        )
    }
}
