package net.primal.domain.profile

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.publisher.NostrPublishException

interface MutedUserRepository {

    fun observeMutedUsersByOwnerId(ownerId: String): Flow<List<ProfileData>>

    fun observeIsUserMutedByOwnerId(pubkey: String, ownerId: String): Flow<Boolean>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchAndPersistMuteList(userId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
    )
    suspend fun muteUserAndPersistMuteList(userId: String, mutedUserId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
    )
    suspend fun unmuteUserAndPersistMuteList(userId: String, unmutedUserId: String)
}
