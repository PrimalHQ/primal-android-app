package net.primal.domain.repository

import kotlinx.coroutines.flow.Flow
import net.primal.domain.DvmFeed
import net.primal.domain.FeedSpecKind
import net.primal.domain.model.PrimalFeed

interface FeedsRepository {

    fun observeAllFeeds(userId: String): Flow<List<PrimalFeed>>

    fun observeReadsFeeds(userId: String): Flow<List<PrimalFeed>>

    fun observeNotesFeeds(userId: String): Flow<List<PrimalFeed>>

    fun observeFeeds(userId: String, specKind: FeedSpecKind): Flow<List<PrimalFeed>>

    fun observeContainsFeedSpec(userId: String, feedSpec: String): Flow<Boolean>

    suspend fun fetchAndPersistArticleFeeds(userId: String)

    suspend fun fetchAndPersistNoteFeeds(userId: String)

    suspend fun persistNewDefaultFeeds(
        userId: String,
        specKind: FeedSpecKind,
        givenDefaultFeeds: List<PrimalFeed>,
    )

    suspend fun fetchDefaultFeeds(userId: String, specKind: FeedSpecKind): List<PrimalFeed>?

    suspend fun persistRemotelyAllLocalUserFeeds(userId: String)

    suspend fun persistLocallyAndRemotelyUserFeeds(
        userId: String,
        specKind: FeedSpecKind,
        feeds: List<PrimalFeed>,
    )

    suspend fun fetchAndPersistDefaultFeeds(
        userId: String,
        specKind: FeedSpecKind,
        givenDefaultFeeds: List<PrimalFeed>,
    )

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
