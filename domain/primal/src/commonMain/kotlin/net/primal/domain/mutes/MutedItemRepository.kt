package net.primal.domain.mutes

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.publisher.NostrPublishException
import net.primal.domain.profile.ProfileData

interface MutedItemRepository {
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

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
    )
    suspend fun muteThreadAndPersistMuteList(userId: String, postId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
    )
    suspend fun unmuteThreadAndPersistMuteList(userId: String, postId: String)
}
