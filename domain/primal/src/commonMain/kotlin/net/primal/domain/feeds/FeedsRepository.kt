package net.primal.domain.feeds

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException

interface FeedsRepository {

    fun observeAllFeeds(userId: String): Flow<List<PrimalFeed>>

    fun observeReadsFeeds(userId: String): Flow<List<PrimalFeed>>

    fun observeNotesFeeds(userId: String): Flow<List<PrimalFeed>>

    fun observeFeeds(userId: String, specKind: FeedSpecKind): Flow<List<PrimalFeed>>

    fun observeContainsFeedSpec(userId: String, feedSpec: String): Flow<Boolean>

    @Throws(
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun fetchAndPersistArticleFeeds(userId: String)

    @Throws(
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun fetchAndPersistNoteFeeds(userId: String)

    @Throws(
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun persistNewDefaultFeeds(
        userId: String,
        specKind: FeedSpecKind,
        givenDefaultFeeds: List<PrimalFeed>,
    )

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchDefaultFeeds(userId: String, specKind: FeedSpecKind): List<PrimalFeed>?

    @Throws(
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun persistRemotelyAllLocalUserFeeds(userId: String)

    @Throws(
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun persistLocallyAndRemotelyUserFeeds(
        userId: String,
        specKind: FeedSpecKind,
        feeds: List<PrimalFeed>,
    )

    @Throws(
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun fetchAndPersistDefaultFeeds(
        userId: String,
        specKind: FeedSpecKind,
        givenDefaultFeeds: List<PrimalFeed>,
    )

    @Throws(NetworkException::class, CancellationException::class)
    suspend fun fetchRecommendedDvmFeeds(userId: String, specKind: FeedSpecKind? = null): List<DvmFeed>

    suspend fun addDvmFeedLocally(
        userId: String,
        dvmFeed: DvmFeed,
        specKind: FeedSpecKind,
    )

    suspend fun addFeedLocally(
        userId: String,
        feedSpec: String,
        title: String,
        description: String,
        feedSpecKind: FeedSpecKind,
        feedKind: String,
    )

    suspend fun removeFeedLocally(userId: String, feedSpec: String)
}
