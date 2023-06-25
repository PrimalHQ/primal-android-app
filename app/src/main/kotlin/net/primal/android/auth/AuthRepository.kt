package net.primal.android.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.nostr.ext.asEllipsizedNpub
import net.primal.android.nostr.ext.asProfileMetadata
import net.primal.android.nostr.ext.displayNameUiFriendly
import net.primal.android.nostr.ext.takeContentAsUserProfileStatsOrNull
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.api.UsersApi
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
    private val usersApi: UsersApi,
) {

    suspend fun login(nsec: String): String {
        val pubkey = credentialsStore.save(nsec)

        val response = withContext(Dispatchers.IO) { usersApi.getUserProfile(pubkey = pubkey) }
        val profileMetadata = response.metadata?.asProfileMetadata()
        val userProfileStats = response.profileStats?.takeContentAsUserProfileStatsOrNull()
        activeAccountStore.setActiveUserAccount(
            userAccount = UserAccount(
                pubkey = pubkey,
                displayName = profileMetadata?.displayNameUiFriendly() ?: pubkey.asEllipsizedNpub(),
                pictureUrl = profileMetadata?.picture,
                internetIdentifier = profileMetadata?.internetIdentifier,
                followersCount = userProfileStats?.followersCount,
                followingCount = userProfileStats?.followsCount,
                notesCount = userProfileStats?.noteCount,
            )
        )

        return pubkey
    }

    suspend fun logout() {
        activeAccountStore.clearActiveUserAccount()
        credentialsStore.clearCredentials()
    }
}
