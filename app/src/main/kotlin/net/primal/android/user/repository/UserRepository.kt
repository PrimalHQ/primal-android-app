package net.primal.android.user.repository

import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.notifications.repository.NotificationRepository
import net.primal.android.user.accounts.UserAccountFetcher
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.copyContactsIfNotNull
import net.primal.android.user.accounts.copyIfNotNull
import net.primal.android.user.domain.NostrWallet
import net.primal.android.user.domain.UserAccount
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userAccountFetcher: UserAccountFetcher,
    private val accountsStore: UserAccountsStore,
    private val notificationRepository: NotificationRepository,
) {

    suspend fun createNewUserAccount(userId: String): UserAccount {
        val account = UserAccount.buildLocal(pubkey = userId)
        accountsStore.upsertAccount(account)
        return account
    }

    suspend fun fetchAndUpdateUserAccount(userId: String): UserAccount {
        val userProfile = userAccountFetcher.fetchUserProfile(pubkey = userId)
        val userContacts = userAccountFetcher.fetchUserContacts(pubkey = userId)
        return accountsStore.getAndUpdateAccount(userId = userId) {
            copyIfNotNull(
                profile = userProfile,
                contacts = userContacts,
            )
        }
    }

    suspend fun updateContacts(userId: String, contactsUserAccount: UserAccount) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copyContactsIfNotNull(contacts = contactsUserAccount)
                .copy(followingCount = contactsUserAccount.following.size)
        }
    }

    suspend fun connectNostrWallet(userId: String, nostrWalletConnect: NostrWallet) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(nostrWallet = nostrWalletConnect)
        }
    }

    suspend fun disconnectNostrWallet(userId: String) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(nostrWallet = null)
        }
    }

    suspend fun removeAllUserAccounts() {
        accountsStore.clearAllAccounts()
    }

    suspend fun refreshBadges(userId: String) {
        val notificationsCount = try {
            notificationRepository.fetchNotificationsSummary(userId = userId)?.count
        } catch (error: WssException) {
            null
        }

        if (notificationsCount != null) {
            accountsStore.getAndUpdateAccount(userId = userId) {
                this.copy(
                    badges = this.badges.copy(
                        notifications = notificationsCount
                    )
                )
            }
        }
    }
}
