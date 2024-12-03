package net.primal.android.user.accounts

import androidx.room.withTransaction
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.asProfileStatsPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.asUserAccountFromFollowListEvent

class UserAccountFetcher @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val usersApi: UsersApi,
    private val primalDatabase: PrimalDatabase,
) {

    suspend fun fetchUserProfileOrNull(userId: String): UserAccount? {
        val userProfileResponse = withContext(dispatcherProvider.io()) {
            usersApi.getUserProfile(userId = userId)
        }
        val cdnResources = userProfileResponse.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val primalUserName = userProfileResponse.primalUserNames.parseAndMapPrimalUserNames()
        val primalLegendProfiles = userProfileResponse.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
        val profileData = userProfileResponse.metadata?.asProfileDataPO(
            cdnResources = cdnResources,
            primalUserNames = primalUserName,
            primalLegendProfiles = primalLegendProfiles,
        ) ?: return null
        val profileStats = userProfileResponse.profileStats?.asProfileStatsPO()

        withContext(dispatcherProvider.io()) {
            primalDatabase.withTransaction {
                primalDatabase.profiles().insertOrUpdateAll(data = listOf(profileData))
                profileStats?.let(primalDatabase.profileStats()::upsert)
            }
        }

        return UserAccount(
            pubkey = userId,
            authorDisplayName = profileData.authorNameUiFriendly(),
            userDisplayName = profileData.usernameUiFriendly(),
            avatarCdnImage = profileData.avatarCdnImage,
            internetIdentifier = profileData.internetIdentifier,
            lightningAddress = profileData.lightningAddress,
            followersCount = profileStats?.followers,
            followingCount = profileStats?.following,
            notesCount = profileStats?.notesCount,
            repliesCount = profileStats?.repliesCount,
            customBadge = profileData.primalLegendProfile?.customBadge == true,
            avatarRing = profileData.primalLegendProfile?.avatarGlow == true,
            legendaryStyle = LegendaryStyle.valueById(profileData.primalLegendProfile?.styleId),
        )
    }

    suspend fun fetchUserFollowListOrNull(userId: String): UserAccount? {
        val contactsResponse = withContext(dispatcherProvider.io()) {
            usersApi.getUserFollowList(userId = userId)
        }

        return contactsResponse.followListEvent?.asUserAccountFromFollowListEvent()
    }
}
