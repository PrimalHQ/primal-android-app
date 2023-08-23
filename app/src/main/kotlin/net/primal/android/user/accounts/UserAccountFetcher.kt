package net.primal.android.user.accounts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.nostr.ext.asProfileMetadataPO
import net.primal.android.nostr.ext.takeContentAsUserProfileStatsOrNull
import net.primal.android.profile.db.authorNameUiFriendly
import net.primal.android.profile.db.userNameUiFriendly
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.asUserAccount
import javax.inject.Inject

class UserAccountFetcher @Inject constructor(
    private val usersApi: UsersApi
) {

    suspend fun fetchUserProfile(pubkey: String): UserAccount {
        val userProfileResponse = withContext(Dispatchers.IO) {
            usersApi.getUserProfile(pubkey = pubkey)
        }
        val profileMetadata = userProfileResponse.metadata?.asProfileMetadataPO()
        val userProfileStats = userProfileResponse.profileStats?.takeContentAsUserProfileStatsOrNull()

        return UserAccount(
            pubkey = pubkey,
            authorDisplayName = profileMetadata?.authorNameUiFriendly() ?: pubkey.asEllipsizedNpub(),
            userDisplayName = profileMetadata?.userNameUiFriendly() ?: pubkey.asEllipsizedNpub(),
            pictureUrl = profileMetadata?.picture,
            internetIdentifier = profileMetadata?.internetIdentifier,
            lightningAddress = profileMetadata?.lightningAddress,
            followersCount = userProfileStats?.followersCount,
            followingCount = userProfileStats?.followsCount,
            notesCount = userProfileStats?.noteCount,
        )
    }

    suspend fun fetchUserContacts(pubkey: String): UserAccount? {
        val contactsResponse = withContext(Dispatchers.IO) {
            usersApi.getUserContacts(pubkey = pubkey, extendedResponse = false)
        }

        return contactsResponse.contactsEvent?.asUserAccount()
    }
}
