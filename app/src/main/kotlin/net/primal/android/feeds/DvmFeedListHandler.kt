package net.primal.android.feeds

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.primal.android.core.ext.asMapByKey
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.core.networking.utils.retryNetworkCall
import net.primal.domain.events.EventRepository
import net.primal.domain.events.NostrEventStats
import net.primal.domain.events.NostrEventUserStats
import net.primal.domain.feeds.DvmFeed
import net.primal.domain.feeds.FeedSpecKind
import net.primal.domain.feeds.FeedsRepository
import net.primal.domain.profile.ProfileData
import net.primal.domain.profile.ProfileRepository

class DvmFeedListHandler @Inject constructor(
    private val feedsRepository: FeedsRepository,
    private val eventRepository: EventRepository,
    private val profileRepository: ProfileRepository,
) {

    suspend fun fetchDvmFeedsAndObserveStatsUpdates(
        scope: CoroutineScope,
        userId: String,
        specKind: FeedSpecKind? = null,
        update: (List<DvmFeedUi>) -> Unit,
    ) {
        scope.launch {
            var statsJob: Job? = null
            feedsRepository.observeRecommendedDvmFeeds(userId = userId, specKind = specKind)
                .collect { dvmFeeds ->
                    statsJob?.cancel()
                    statsJob = scope.launch {
                        observeStatsAndEmitUpdates(
                            scope = this,
                            dvmFeeds = dvmFeeds,
                            userId = userId,
                            update = update,
                        )
                    }
                }
        }

        retryNetworkCall {
            feedsRepository.fetchRecommendedDvmFeeds(userId = userId, specKind = specKind)
        }
    }

    private suspend fun observeStatsAndEmitUpdates(
        scope: CoroutineScope,
        dvmFeeds: List<DvmFeed>,
        userId: String,
        update: (List<DvmFeedUi>) -> Unit,
    ) {
        val dvmIds = dvmFeeds.map { it.eventId }
        val featuredUserIds = dvmFeeds.flatMap { it.featuredUserIds }.distinct()
        val profilesByUserId = profileRepository.findProfileData(profileIds = featuredUserIds)
            .associateBy { it.profileId }

        var feeds = buildDvmFeedUis(
            dvmFeeds = dvmFeeds,
            statsByEventId = emptyMap(),
            userStatsByEventId = emptyMap(),
            profilesByUserId = profilesByUserId,
        )
        update(feeds)

        scope.launch {
            eventRepository.observeEventStats(eventIds = dvmIds).collect { stats ->
                val statsMap = stats.asMapByKey { it.eventId }
                feeds = feeds.map {
                    it.copy(
                        totalLikes = statsMap[it.data.eventId]?.likes,
                        totalSatsZapped = statsMap[it.data.eventId]?.satsZapped,
                    )
                }
                update(feeds)
            }
        }
        scope.launch {
            eventRepository.observeUserEventStatus(eventIds = dvmIds, userId = userId).collect { userStats ->
                val userStatsMap = userStats.asMapByKey { it.eventId }
                feeds = feeds.map {
                    it.copy(
                        userLiked = userStatsMap[it.data.eventId]?.liked,
                        userZapped = userStatsMap[it.data.eventId]?.zapped,
                    )
                }
                update(feeds)
            }
        }
    }

    private fun buildDvmFeedUis(
        dvmFeeds: List<DvmFeed>,
        statsByEventId: Map<String, NostrEventStats>,
        userStatsByEventId: Map<String, NostrEventUserStats>,
        profilesByUserId: Map<String, ProfileData>,
    ): List<DvmFeedUi> =
        dvmFeeds.map { dvmFeed ->
            val featuredUsers = dvmFeed.featuredUserIds.mapNotNull { profilesByUserId[it] }
            val avatarLegendaryPair = featuredUsers
                .filter { it.avatarCdnImage != null }
                .map { Pair(it.avatarCdnImage, it.primalPremiumInfo?.legendProfile?.asLegendaryCustomization()) }

            DvmFeedUi(
                data = dvmFeed,
                userLiked = userStatsByEventId[dvmFeed.eventId]?.liked,
                userZapped = userStatsByEventId[dvmFeed.eventId]?.zapped,
                totalLikes = statsByEventId[dvmFeed.eventId]?.likes,
                totalSatsZapped = statsByEventId[dvmFeed.eventId]?.satsZapped,
                featuredUserAvatars = avatarLegendaryPair.mapNotNull { it.first },
                featuredUserLegendaryCustomizations = avatarLegendaryPair.map { it.second },
            )
        }
}
