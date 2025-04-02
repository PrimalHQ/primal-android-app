package net.primal.android.user.accounts

import javax.inject.Inject
import net.primal.android.user.domain.UserAccount
import net.primal.core.utils.coroutines.DispatcherProvider

// TODO Reimplement UserAccountFetcher to use repositories
class UserAccountFetcher @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
) {

    suspend fun fetchUserProfileOrNull(userId: String): UserAccount? {
        return UserAccount.EMPTY

//        val userProfileResponse = withContext(dispatcherProvider.io()) {
//            usersApi.getUserProfile(userId = userId)
//        }
//        val cdnResources = userProfileResponse.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
//        val primalUserName = userProfileResponse.primalUserNames.parseAndMapPrimalUserNames()
//        val primalPremiumInfo = userProfileResponse.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
//        val primalLegendProfiles = userProfileResponse.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
//        val blossomServers = userProfileResponse.blossomServers.mapAsMapPubkeyToListOfBlossomServers()
//        val profileData = userProfileResponse.metadata?.asProfileDataPO(
//            cdnResources = cdnResources,
//            primalUserNames = primalUserName,
//            primalPremiumInfo = primalPremiumInfo,
//            primalLegendProfiles = primalLegendProfiles,
//            blossomServers = blossomServers,
//        ) ?: return null
//        val profileStats = userProfileResponse.profileStats?.asProfileStatsPO()
//
//        withContext(dispatcherProvider.io()) {
//            primalDatabase.withTransaction {
//                primalDatabase.profiles().insertOrUpdateAll(data = listOf(profileData))
//                profileStats?.let(primalDatabase.profileStats()::upsert)
//            }
//        }
//
//        return UserAccount(
//            pubkey = userId,
//            authorDisplayName = profileData.authorNameUiFriendly(),
//            userDisplayName = profileData.usernameUiFriendly(),
//            avatarCdnImage = profileData.avatarCdnImage,
//            internetIdentifier = profileData.internetIdentifier,
//            lightningAddress = profileData.lightningAddress,
//            followersCount = profileStats?.followers,
//            followingCount = profileStats?.following,
//            notesCount = profileStats?.notesCount,
//            repliesCount = profileStats?.repliesCount,
//            primalLegendProfile = profileData.primalPremiumInfo?.legendProfile,
//        )
    }

    suspend fun fetchUserFollowListOrNull(userId: String): UserAccount? {
        return UserAccount.EMPTY
//        val contactsResponse = withContext(dispatcherProvider.io()) {
//            usersApi.getUserFollowList(userId = userId)
//        }
//
//        return contactsResponse.followListEvent?.asUserAccountFromFollowListEvent()
    }
}
