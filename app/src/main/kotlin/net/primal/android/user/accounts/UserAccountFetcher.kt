package net.primal.android.user.accounts

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.nostr.ext.asProfileMetadata
import net.primal.android.nostr.ext.takeContentAsUserProfileStatsOrNull
import net.primal.android.profile.db.displayNameUiFriendly
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.Relay
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject

class UserAccountFetcher @Inject constructor(
    private val usersApi: UsersApi
) {

    suspend fun fetchUserProfile(pubkey: String): UserAccount {
        val userProfileResponse = withContext(Dispatchers.IO) {
            usersApi.getUserProfile(pubkey = pubkey)
        }
        val profileMetadata = userProfileResponse.metadata?.asProfileMetadata()
        val userProfileStats = userProfileResponse.profileStats?.takeContentAsUserProfileStatsOrNull()

        return UserAccount(
            pubkey = pubkey,
            displayName = profileMetadata?.displayNameUiFriendly() ?: pubkey.asEllipsizedNpub(),
            pictureUrl = profileMetadata?.picture,
            internetIdentifier = profileMetadata?.internetIdentifier,
            followersCount = userProfileStats?.followersCount,
            followingCount = userProfileStats?.followsCount,
            notesCount = userProfileStats?.noteCount,
        )
    }

    suspend fun fetchUserContacts(pubkey: String): UserAccount {
        val contactsResponse = withContext(Dispatchers.IO) {
            usersApi.getUserContacts(pubkey = pubkey)
        }

        val userRelays = contactsResponse.contactsEvent?.content?.parseRelays()
        val following = contactsResponse.contactsEvent?.tags?.parseFollowings()
        val interests = contactsResponse.contactsEvent?.tags?.parseInterests()

        val finalRelays = if (userRelays.isNullOrEmpty()) {
            BOOTSTRAP_RELAYS.map { Relay(url = it, read = true, write = true) }
        } else userRelays

        return UserAccount(
            pubkey = pubkey,
            displayName = pubkey.asEllipsizedNpub(),
            relays = finalRelays,
            following = following ?: emptyList(),
            interests = interests ?: emptyList(),
        )
    }

}
