package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.feeds.DvmFeedActionUserCrossRef
import net.primal.data.local.dao.feeds.DvmFeedData
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

internal fun DvmFeed.asActionCrossRefs(ownerId: String): List<DvmFeedActionUserCrossRef> =
    actionUserIds.map { profileId ->
        DvmFeedActionUserCrossRef(
            ownerId = ownerId,
            dvmEventId = eventId,
            profileId = profileId,
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

internal fun DvmFeedData.asDvmFeedDO(actionUserIds: List<String>): DvmFeed =
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
        actionUserIds = actionUserIds,
    )
