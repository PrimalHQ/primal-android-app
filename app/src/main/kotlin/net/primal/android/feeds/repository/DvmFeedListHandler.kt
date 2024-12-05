package net.primal.android.feeds.repository

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.primal.android.core.ext.asMapByKey
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.networking.primal.retryNetworkCall
import net.primal.android.premium.legend.asLegendaryCustomization
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.stats.repository.EventRepository

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
        val dvmFeeds = retryNetworkCall {
            feedsRepository.fetchRecommendedDvmFeeds(userId = userId, specKind = specKind)
        }
        val dvmIds = dvmFeeds.map { it.eventId }
        val stats = eventRepository.observeEventStats(eventIds = dvmIds).first().asMapByKey { it.eventId }
        val userStats = eventRepository.observeUserEventStatus(userId = userId, eventIds = dvmIds)
            .first()
            .asMapByKey { it.eventId }

        var feeds = dvmFeeds.map { dvmFeed ->
            val actionUsers = profileRepository.findProfilesData(profileIds = dvmFeed.actionUserIds)
            val avatarLegendaryCustomizationPair = actionUsers
                .filter { it.avatarCdnImage != null }
                .map { Pair(it.avatarCdnImage, it.primalPremiumInfo?.legendProfile?.asLegendaryCustomization()) }

            DvmFeedUi(
                data = dvmFeed,
                userLiked = userStats[dvmFeed.eventId]?.liked,
                userZapped = userStats[dvmFeed.eventId]?.zapped,
                totalLikes = stats[dvmFeed.eventId]?.likes,
                totalSatsZapped = stats[dvmFeed.eventId]?.satsZapped,
                actionUserAvatars = avatarLegendaryCustomizationPair.mapNotNull { it.first },
                actionUserLegendaryCustomizations = avatarLegendaryCustomizationPair.map { it.second },
            )
        }
        update(feeds)

        scope.launch {
            eventRepository.observeUserEventStatus(
                userId = userId,
                eventIds = feeds.map {
                    it.data.eventId
                },
            ).collect { eventStats ->
                val dvmStats = eventStats.asMapByKey { it.eventId }
                feeds = feeds.map {
                    it.copy(
                        userLiked = dvmStats[it.data.eventId]?.liked,
                        userZapped = dvmStats[it.data.eventId]?.zapped,
                    )
                }
                update(feeds)
            }
        }

        scope.launch {
            eventRepository.observeEventStats(eventIds = feeds.map { it.data.eventId }).collect { eventStats ->
                val dvmStats = eventStats.asMapByKey { it.eventId }
                feeds = feeds.map {
                    it.copy(
                        totalLikes = dvmStats[it.data.eventId]?.likes,
                        totalSatsZapped = dvmStats[it.data.eventId]?.satsZapped,
                    )
                }
                update(feeds)
            }
        }
    }
}
