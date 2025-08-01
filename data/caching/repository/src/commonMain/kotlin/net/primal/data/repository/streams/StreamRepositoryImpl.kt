package net.primal.data.repository.streams

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.repository.mappers.local.asStreamDO
import net.primal.data.repository.mappers.remote.extractZapRequestOrNull
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.streams.Stream
import net.primal.domain.streams.StreamRepository

class StreamRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val database: PrimalDatabase,
) : StreamRepository {

    override suspend fun findLatestLiveStreamATag(authorId: String): String? {
        val streamsPO = database.streams().observeStreamsByAuthorId(authorId).first()
        val liveStreamPO = streamsPO.find { it.data.isLive() }
        return liveStreamPO?.data?.aTag
    }

    override fun observeStream(aTag: String): Flow<Stream?> {
        return database.streams().observeStreamByATag(aTag = aTag)
            .map { streamPO ->
                streamPO?.asStreamDO()
            }
    }

    override suspend fun saveZap(zapEvent: NostrEvent, zappedEventATag: String) =
        withContext(dispatcherProvider.io()) {
            val zapRequest = zapEvent.extractZapRequestOrNull() ?: run {
                Napier.w("Could not extract zap request from zap event ${zapEvent.id}")
                return@withContext
            }

            val zapperPubkey = zapRequest.pubKey
            val zapperProfile = database.profiles().findProfileData(profileId = zapperPubkey)

            val profiles = if (zapperProfile != null) listOf(zapperProfile) else emptyList()
            val profilesMap = profiles.associateBy { it.ownerId }

            val eventZaps = listOf(zapEvent).mapAsEventZapDO(profilesMap = profilesMap)

            if (eventZaps.isNotEmpty()) {
                database.eventZaps().upsertAll(data = eventZaps)
            }
        }
}
