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

    fun observeMutedHashtagsByOwnerId(ownerId: String): Flow<List<String>>

    fun observeMutedWordsByOwnerId(ownerId: String): Flow<List<String>>

    fun observeMutedProfileIdsByOwnerId(ownerId: String): Flow<List<String>>

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchAndPersistMuteList(userId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
        NetworkException::class,
    )
    suspend fun muteUserAndPersistMuteList(userId: String, mutedUserId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
        NetworkException::class,
    )
    suspend fun unmuteUserAndPersistMuteList(userId: String, unmutedUserId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
        NetworkException::class,
    )
    suspend fun muteThreadAndPersistMuteList(userId: String, postId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
        NetworkException::class,
    )
    suspend fun unmuteThreadAndPersistMuteList(userId: String, postId: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
        NetworkException::class,
    )
    suspend fun muteHashtagAndPersistMuteList(userId: String, hashtag: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
        NetworkException::class,
    )
    suspend fun unmuteHashtagAndPersistMuteList(userId: String, hashtag: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
        NetworkException::class,
    )
    suspend fun muteWordAndPersistMuteList(userId: String, word: String)

    @Throws(
        MissingRelaysException::class,
        NostrPublishException::class,
        SignatureException::class,
        CancellationException::class,
        NetworkException::class,
    )
    suspend fun unmuteWordAndPersistMuteList(userId: String, word: String)
}
