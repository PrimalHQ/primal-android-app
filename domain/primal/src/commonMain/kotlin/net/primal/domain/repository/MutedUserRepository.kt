package net.primal.domain.repository

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.model.ProfileData
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.publisher.NostrPublishException

interface MutedUserRepository {

    fun observeMutedUsersByOwnerId(ownerId: String): Flow<List<ProfileData>>

    fun observeIsUserMutedByOwnerId(pubkey: String, ownerId: String): Flow<Boolean>

    suspend fun fetchAndPersistMuteList(userId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SigningRejectedException::class,
        SigningKeyNotFoundException::class,
        CancellationException::class,
    )
    suspend fun muteUserAndPersistMuteList(userId: String, mutedUserId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SigningRejectedException::class,
        SigningKeyNotFoundException::class,
        CancellationException::class,
    )
    suspend fun unmuteUserAndPersistMuteList(userId: String, unmutedUserId: String)
}
