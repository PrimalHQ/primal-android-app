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
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.repository.PublicBookmarksRepository
import timber.log.Timber

class UserDataUpdater @AssistedInject constructor(
    @Assisted val userId: String,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository,
    private val relayRepository: RelayRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val premiumRepository: PremiumRepository,
    private val nostrNotary: NostrNotary,
    private val pushNotificationsTokenUpdater: PushNotificationsTokenUpdater,
) {

    private var lastTimeFetched: Instant = Instant.EPOCH

    private fun isUserDataSyncedInLast(duration: Duration): Boolean {
        return lastTimeFetched < Instant.now().minusMillis(duration.inWholeMilliseconds)
    }

    suspend fun updateUserDataWithDebounce(duration: Duration) {
        if (isUserDataSyncedInLast(duration)) {
            try {
                updateData()
                lastTimeFetched = Instant.now()
            } catch (error: WssException) {
                Timber.w(error)
            } catch (error: SignatureException) {
                Timber.w(error)
            }
        }
    }

    private suspend fun updateData() {
        val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
            userId = userId,
            description = "Sync app settings",
        )
        settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
        settingsRepository.ensureZapConfig(authorizationEvent) { appSettings ->
            nostrNotary.signAppSettingsNostrEvent(
                userId = userId,
                appSettings = appSettings,
            )
        }
        premiumRepository.fetchMembershipStatus(userId = userId)
        relayRepository.fetchAndUpdateUserRelays(userId = userId)
        userRepository.fetchAndUpdateUserAccount(userId = userId)
        bookmarksRepository.fetchAndPersistBookmarks(userId = userId)
        walletRepository.fetchUserWalletInfoAndUpdateUserAccount(userId = userId)
        pushNotificationsTokenUpdater.updateTokenForAllUsers()
    }
}
