package net.primal.android.messages.api.mediator

import androidx.room.withTransaction
import javax.inject.Inject
import net.primal.android.attachments.ext.flatMapMessagesAsNoteAttachmentPO
import net.primal.android.core.ext.asMapByKey
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.db.PrimalDatabase
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.extractNoteId
import net.primal.android.nostr.ext.extractProfileId
import net.primal.android.nostr.ext.flatMapMessagesAsNostrResourcePO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.isNostrUri
import net.primal.android.nostr.ext.mapAsMessageDataPO
import net.primal.android.nostr.ext.mapAsPostDataPO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.mapNotNullAsPostDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.notes.api.FeedApi
import net.primal.android.notes.repository.persistToDatabaseAsTransaction
import net.primal.android.user.api.UsersApi
import net.primal.android.user.credentials.CredentialsStore
import timber.log.Timber

class MessagesProcessor @Inject constructor(
    private val database: PrimalDatabase,
    private val feedApi: FeedApi,
    private val usersApi: UsersApi,
    private val credentialsStore: CredentialsStore,
) {

    suspend fun processMessageEventsAndSave(
        userId: String,
        messages: List<NostrEvent>,
        profileMetadata: List<NostrEvent>,
        mediaResources: List<PrimalEvent>,
        primalUserNames: PrimalEvent?,
        primalPremiumInfo: PrimalEvent?,
        primalLegendProfiles: PrimalEvent?,
    ) {
        val messageDataList = messages.mapAsMessageDataPO(
            userId = userId,
            nsec = credentialsStore.findOrThrow(npub = userId.hexToNpubHrp()).nsec,
        )

        processNostrUrisAndSave(userId = userId, messageDataList = messageDataList)

        val cdnResources = mediaResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val primalUserNamesMap = primalUserNames.parseAndMapPrimalUserNames()
        val primalPremiumInfoMap = primalPremiumInfo.parseAndMapPrimalPremiumInfo()
        val primalLegendProfilesMap = primalLegendProfiles.parseAndMapPrimalLegendProfiles()
        val attachments = messageDataList.flatMapMessagesAsNoteAttachmentPO()

        database.withTransaction {
            database.profiles().insertOrUpdateAll(
                data = profileMetadata.mapAsProfileDataPO(
                    cdnResources = cdnResources,
                    primalUserNames = primalUserNamesMap,
                    primalPremiumInfo = primalPremiumInfoMap,
                    primalLegendProfiles = primalLegendProfilesMap,
                ),
            )
            database.messages().upsertAll(data = messageDataList)
            database.attachments().upsertAllNoteAttachments(data = attachments)
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
                response.posts.mapAsPostDataPO(referencedPosts = referencedPostsWithReplies)
            } catch (error: WssException) {
                Timber.w(error)
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
                val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
                val profiles = response.metadataEvents.mapAsProfileDataPO(
                    cdnResources = cdnResources,
                    primalUserNames = primalUserNames,
                    primalPremiumInfo = primalPremiumInfo,
                    primalLegendProfiles = primalLegendProfiles,
                )
                database.profiles().insertOrUpdateAll(data = profiles)
                profiles
            } catch (error: WssException) {
                Timber.w(error)
                emptyList()
            }
        } else {
            emptyList()
        }

        val referencedProfilesMap = (localProfiles + remoteProfiles)
            .groupBy { it.ownerId }
            .mapValues { it.value.first() }

        database.attachments().upsertAllNostrUris(
            data = messageDataList.flatMapMessagesAsNostrResourcePO(
                eventIdToNostrEvent = emptyMap(),
                postIdToPostDataMap = referencedNotesMap,
                articleIdToArticle = emptyMap(),
                profileIdToProfileDataMap = referencedProfilesMap,
                cdnResources = emptyMap(),
                linkPreviews = emptyMap(),
                videoThumbnails = emptyMap(),
            ),
        )
    }
}
