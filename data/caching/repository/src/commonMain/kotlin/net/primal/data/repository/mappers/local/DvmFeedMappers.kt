package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.feeds.DvmFeedData
import net.primal.data.local.dao.feeds.DvmFeedFeaturedUserCrossRef
import net.primal.data.local.dao.feeds.RecommendedDvmFeedCrossRef
import net.primal.data.local.dao.feeds.asSpecKindFilter
import net.primal.domain.feeds.DvmFeed
import net.primal.domain.feeds.FeedSpecKind

internal fun DvmFeed.asDvmFeedPO(): DvmFeedData =
    DvmFeedData(
        eventId = eventId,
        dvmId = dvmId,
        dvmPubkey = dvmPubkey,
        dvmLnUrl = dvmLnUrlDecoded,
        avatarUrl = avatarUrl,
        title = title,
        description = description,
        amountInSats = amountInSats,
        primalSubscriptionRequired = primalSubscriptionRequired,
        kind = kind,
        primalSpec = primalSpec,
        isPrimalFeed = isPrimalFeed,
    )

internal fun DvmFeed.asFeaturedCrossRefs(ownerId: String): List<DvmFeedFeaturedUserCrossRef> =
    featuredUserIds.mapIndexed { index, profileId ->
        DvmFeedFeaturedUserCrossRef(
            ownerId = ownerId,
            dvmEventId = eventId,
            profileId = profileId,
            position = index,
        )
    }

internal fun List<DvmFeed>.asRecommendedCrossRefs(
    ownerId: String,
    specKind: FeedSpecKind?,
): List<RecommendedDvmFeedCrossRef> {
    val filter = specKind.asSpecKindFilter()
    return mapIndexed { index, feed ->
        RecommendedDvmFeedCrossRef(
            ownerId = ownerId,
            dvmEventId = feed.eventId,
            specKindFilter = filter,
            position = index,
        )
    }
}

internal fun DvmFeedData.asDvmFeedDO(featuredUserIds: List<String>): DvmFeed =
    DvmFeed(
        eventId = eventId,
        dvmId = dvmId,
        dvmPubkey = dvmPubkey,
        dvmLnUrlDecoded = dvmLnUrl,
        avatarUrl = avatarUrl,
        title = title,
        description = description,
        amountInSats = amountInSats,
        primalSubscriptionRequired = primalSubscriptionRequired,
        kind = kind,
        primalSpec = primalSpec,
        isPrimalFeed = isPrimalFeed,
        featuredUserIds = featuredUserIds,
    )
