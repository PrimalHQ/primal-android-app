package net.primal.android.messages.api.mediator

import androidx.room.withTransaction
import javax.inject.Inject
import net.primal.android.attachments.ext.flatMapMessagesAsNoteAttachmentPO
import net.primal.android.core.ext.asMapByKey
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.repository.persistToDatabaseAsTransaction
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
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent
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
    ) {
        val messageDataList = messages.mapAsMessageDataPO(
            userId = userId,
            nsec = credentialsStore.findOrThrow(npub = userId.hexToNpubHrp()).nsec,
        )

        processNostrUrisAndSave(userId = userId, messageDataList = messageDataList)

        val cdnResources = mediaResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val attachments = messageDataList.flatMapMessagesAsNoteAttachmentPO()

        database.withTransaction {
            database.profiles().upsertAll(data = profileMetadata.mapAsProfileDataPO(cdnResources = cdnResources))
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
                response.posts.mapAsPostDataPO()
            } catch (error: WssException) {
                Timber.e(error)
                emptyList()
            }
        } else {
            emptyList()
        }

        val allNotes = (localNotes + remoteNotes)
        val referencedNotesMap = allNotes
            .groupBy { it.postId }
            .mapValues { it.value.first() }

        val referencedProfileIds = nostrUris.mapNotNull { it.extractProfileId() }.toSet()
        val refNoteAuthorProfileIds = allNotes.map { it.authorId }.toSet()
        val allProfileIds = referencedProfileIds + refNoteAuthorProfileIds
        val localProfiles = database.profiles().findProfileData(allProfileIds.toList())
        val missingProfileIds = allProfileIds - localProfiles.map { it.ownerId }.toSet()
        val remoteProfiles = if (missingProfileIds.isNotEmpty()) {
            try {
                val response = usersApi.getUserProfilesMetadata(userIds = missingProfileIds)
                val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
                val profiles = response.metadataEvents.mapAsProfileDataPO(cdnResources = cdnResources)
                database.profiles().upsertAll(data = profiles)
                profiles
            } catch (error: WssException) {
                Timber.e(error)
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
                postIdToPostDataMap = referencedNotesMap,
                profileIdToProfileDataMap = referencedProfilesMap,
            ),
        )
    }
}
