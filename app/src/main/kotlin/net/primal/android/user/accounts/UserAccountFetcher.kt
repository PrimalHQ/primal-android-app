package net.primal.android.user.accounts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.nostr.ext.asProfileDataPO
import net.primal.android.nostr.ext.takeContentAsUserProfileStatsOrNull
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.asUserAccountFromContactsEvent
import javax.inject.Inject

class UserAccountFetcher @Inject constructor(
    private val usersApi: UsersApi
) {

    suspend fun fetchUserProfile(pubkey: String): UserAccount {
        val userProfileResponse = withContext(Dispatchers.IO) {
            usersApi.getUserProfile(pubkey = pubkey)
        }
        val profileData = userProfileResponse.metadata?.asProfileDataPO()
        val userProfileStats = userProfileResponse.profileStats?.takeContentAsUserProfileStatsOrNull()

        return UserAccount(
            pubkey = pubkey,
            authorDisplayName = profileData?.authorNameUiFriendly() ?: pubkey.asEllipsizedNpub(),
            userDisplayName = profileData?.usernameUiFriendly() ?: pubkey.asEllipsizedNpub(),
            pictureUrl = profileData?.picture,
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
