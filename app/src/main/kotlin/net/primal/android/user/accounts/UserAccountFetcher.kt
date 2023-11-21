package net.primal.android.user.accounts

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.core.ext.asMapByKey
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.takeContentAsUserProfileStatsOrNull
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.asUserAccountFromContactsEvent

class UserAccountFetcher @Inject constructor(
    private val usersApi: UsersApi,
) {

    suspend fun fetchUserProfile(pubkey: String): UserAccount {
        val userProfileResponse = withContext(Dispatchers.IO) {
            usersApi.getUserProfile(pubkey = pubkey)
        }
        val cdnResources = userProfileResponse.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
        val profileData = userProfileResponse.metadata?.asProfileDataPO(cdnResources = cdnResources)
        val userProfileStats = userProfileResponse.profileStats?.takeContentAsUserProfileStatsOrNull()

        return UserAccount(
            pubkey = pubkey,
            authorDisplayName = profileData?.authorNameUiFriendly() ?: pubkey.asEllipsizedNpub(),
            userDisplayName = profileData?.usernameUiFriendly() ?: pubkey.asEllipsizedNpub(),
            avatarCdnImage = profileData?.avatarCdnImage,
            internetIdentifier = profileData?.internetIdentifier,
            lightningAddress = profileData?.lightningAddress,
            followersCount = userProfileStats?.followersCount,
            followingCount = userProfileStats?.followsCount,
            notesCount = userProfileStats?.noteCount,
        )
    }

    suspend fun fetchUserContacts(pubkey: String): UserAccount? {
        val contactsResponse = withContext(Dispatchers.IO) {
            usersApi.getUserContacts(pubkey = pubkey, extendedResponse = false)
        }

        return contactsResponse.contactsEvent?.asUserAccountFromContactsEvent()
    }
}
