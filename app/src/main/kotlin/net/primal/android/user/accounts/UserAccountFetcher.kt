package net.primal.android.user.accounts

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.asProfileStatsPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.asUserAccountFromContactsEvent

class UserAccountFetcher @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val usersApi: UsersApi,
) {

    suspend fun fetchUserProfileOrNull(userId: String): UserAccount? {
        val userProfileResponse = withContext(dispatcherProvider.io()) {
            usersApi.getUserProfile(pubkey = userId)
        }
        val cdnResources = userProfileResponse.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profileData = userProfileResponse.metadata?.asProfileDataPO(cdnResources = cdnResources) ?: return null
        val profileStats = userProfileResponse.profileStats?.asProfileStatsPO()

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
        )
    }

    suspend fun fetchUserContactsOrNull(userId: String): UserAccount? {
        val contactsResponse = withContext(dispatcherProvider.io()) {
            usersApi.getUserContacts(pubkey = userId, extendedResponse = false)
        }

        return contactsResponse.contactsEvent?.asUserAccountFromContactsEvent()
    }
}
