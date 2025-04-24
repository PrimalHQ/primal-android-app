package net.primal.android.user.updater

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import kotlin.time.Duration
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.repository.WalletRepository
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow

class UserDataUpdater @AssistedInject constructor(
    @Assisted val userId: String,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository,
    private val relayRepository: RelayRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val premiumRepository: PremiumRepository,
    private val mutedItemRepository: MutedItemRepository,
    private val nostrNotary: NostrNotary,
    private val pushNotificationsTokenUpdater: PushNotificationsTokenUpdater,
) {

    private var lastTimeFetched: Instant = Instant.EPOCH

    private fun isUserDataSyncedInLast(duration: Duration): Boolean {
        return lastTimeFetched < Instant.now().minusMillis(duration.inWholeMilliseconds)
    }

    suspend fun updateUserDataWithDebounce(duration: Duration) {
        if (isUserDataSyncedInLast(duration)) {
            updateData()
            lastTimeFetched = Instant.now()
        }
    }

    private suspend fun updateData() {
        runCatching {
            val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
                userId = userId,
                description = "Sync app settings",
            ).unwrapOrThrow()
            settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
            settingsRepository.ensureZapConfig(authorizationEvent) { appSettings ->
                nostrNotary.signAppSettingsNostrEvent(
                    userId = userId,
                    appSettings = appSettings,
                ).unwrapOrThrow()
            }
        }
        runCatching { premiumRepository.fetchMembershipStatus(userId = userId) }
        runCatching { relayRepository.fetchAndUpdateUserRelays(userId = userId) }
        runCatching { userRepository.fetchAndUpdateUserAccount(userId = userId) }
        runCatching { bookmarksRepository.fetchAndPersistBookmarks(userId = userId) }
        runCatching { walletRepository.fetchUserWalletInfoAndUpdateUserAccount(userId = userId) }
        runCatching { pushNotificationsTokenUpdater.updateTokenForAllUsers() }
        runCatching { mutedItemRepository.fetchAndPersistMuteList(userId = userId) }
    }
}
