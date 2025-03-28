package net.primal.android.user.updater

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import kotlin.time.Duration
import net.primal.android.bookmarks.BookmarksRepository
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.nostr.notary.exceptions.SignException
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.repository.WalletRepository
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

class UserDataUpdater @AssistedInject constructor(
    @Assisted val userId: String,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository,
    private val relayRepository: RelayRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val premiumRepository: PremiumRepository,
    private val nostrNotary: NostrNotary,
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
            } catch (error: SignException) {
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
        bookmarksRepository.fetchAndPersistPublicBookmarks(userId = userId)
        walletRepository.fetchUserWalletInfoAndUpdateUserAccount(userId = userId)
    }
}
