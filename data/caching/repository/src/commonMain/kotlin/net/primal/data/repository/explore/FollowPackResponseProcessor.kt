package net.primal.data.repository.explore

import net.primal.core.networking.utils.orderByPagingIfNotNull
import net.primal.core.utils.asMapByKey
import net.primal.data.local.dao.explore.FollowPackData
import net.primal.data.local.dao.explore.FollowPackProfileCrossRef
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.explore.model.FollowListsResponse
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asFollowPackDO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserFollowersCountsOrNull
import net.primal.data.repository.mappers.remote.takeContentAsPrimalUserScoresOrNull
import net.primal.domain.explore.FollowPack
import net.primal.domain.links.CdnImage
import net.primal.domain.links.CdnResource
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findFirstDescription
import net.primal.domain.nostr.findFirstIdentifier
import net.primal.domain.nostr.findFirstImage
import net.primal.domain.nostr.findFirstTitle
import net.primal.domain.nostr.getTagValueOrNull
import net.primal.domain.nostr.isPubKeyTag
import net.primal.shared.data.local.db.withTransaction

internal suspend fun FollowListsResponse.processAndPersistFollowLists(database: PrimalDatabase): List<FollowPack> {
    val primalUserNames = primalUserNames.parseAndMapPrimalUserNames()
    val primalPremiumInfo = primalPremiumInfo.parseAndMapPrimalPremiumInfo()
    val primalLegendProfiles = primalLegendProfiles.parseAndMapPrimalLegendProfiles()
    val cdnResources = cdnResources.flatMapNotNullAsCdnResource()
    val blossomServers = blossomServers.mapAsMapPubkeyToListOfBlossomServers()
    val profiles = metadata.mapAsProfileDataPO(
        cdnResources = cdnResources,
        primalUserNames = primalUserNames,
        primalPremiumInfo = primalPremiumInfo,
        primalLegendProfiles = primalLegendProfiles,
        blossomServers = blossomServers,
    )
    val profilesMap = profiles.asMapByKey { it.ownerId }
    val userScoresMap = primalUserScores?.takeContentAsPrimalUserScoresOrNull()
    val userFollowCount = primalUserFollowersCounts?.takeContentAsPrimalUserFollowersCountsOrNull()

    val followPacks = followListEvents
        .orderByPagingIfNotNull(pagingEvent = pagingEvent)
        .mapAsFollowPackData(cdnResourcesMap = cdnResources.asMapByKey { it.url })

    database.withTransaction {
        database.profiles().insertOrUpdateAll(data = profiles)
        database.followPacks().upsertFollowPackData(data = followPacks.map { it.first })
        database.followPacks().clearCrossRefsByPackATags(
            aTags = followPacks.map { it.first }.map { it.aTag },
        )
        database.followPacks().upsertCrossRefs(refs = followPacks.buildFollowPackProfileCrossRefs())
        userFollowCount?.forEach { (profileId, followers) ->
            if (profileId.isNotEmpty()) {
                database.profileStats().upsertFollowers(
                    profileId = profileId,
                    followers = followers,
                )
            }
        }
    }

    return followPacks.mapNotNull {
        it.first.asFollowPackDO(
            profiles = profilesMap,
            people = it.second.mapNotNull { profilesMap[it] }
                .sortedBy { userScoresMap?.get(it.ownerId) ?: 0f },
            followersCountMap = userFollowCount ?: emptyMap(),
        )
    }
}

internal fun List<NostrEvent>.mapAsFollowPackData(cdnResourcesMap: Map<String, CdnResource>) =
    this.mapNotNull { nostrEvent ->
        FollowPackData(
            aTag = "${NostrEventKind.StarterPack.value}:${nostrEvent.pubKey}:${nostrEvent.tags.findFirstIdentifier()}",
            identifier = nostrEvent.tags.findFirstIdentifier() ?: return@mapNotNull null,
            title = nostrEvent.tags.findFirstTitle() ?: return@mapNotNull null,
            coverCdnImage = nostrEvent.tags.findFirstImage()?.let {
                CdnImage(
                    sourceUrl = it,
                    variants = cdnResourcesMap[it]?.variants ?: emptyList(),
                )
            },
            description = nostrEvent.tags.findFirstDescription(),
            authorId = nostrEvent.pubKey,
            updatedAt = nostrEvent.createdAt,
            profilesCount = nostrEvent.tags.filter { it.isPubKeyTag() }.size,
        ) to nostrEvent.tags.filter { it.isPubKeyTag() }.mapNotNull { it.getTagValueOrNull() }
    }

private fun List<Pair<FollowPackData, List<String>>>.buildFollowPackProfileCrossRefs() =
    flatMap { pair ->
        pair.second.map { pubkey ->
            FollowPackProfileCrossRef(
                followPackATag = pair.first.aTag,
                profileId = pubkey,
            )
        }
    }
