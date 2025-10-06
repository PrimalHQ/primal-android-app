package net.primal.android.user.updater

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.Clock
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.settings.wallet.domain.WalletPreference
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.repository.RelayRepository
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.Result
import net.primal.core.utils.asSha256Hash
import net.primal.core.utils.updater.Updater
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository

class UserDataUpdater @AssistedInject constructor(
    @Assisted val userId: String,
    private val activeAccountStore: ActiveAccountStore,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val walletAccountRepository: WalletAccountRepository,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val walletRepository: WalletRepository,
    private val relayRepository: RelayRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val premiumRepository: PremiumRepository,
    private val mutedItemRepository: MutedItemRepository,
    private val nostrNotary: NostrNotary,
    private val pushNotificationsTokenUpdater: PushNotificationsTokenUpdater,
) : Updater() {

    override suspend fun doUpdate(): Result<Unit> {
        activeAccountStore.activeUserAccount().let { activeAccount ->
            if (activeAccount.nostrWallet != null || activeAccount.primalWallet != null) {
                runCatching { migrateWalletData(userAccount = activeAccount) }
            }
        }

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
        runCatching { primalWalletAccountRepository.fetchWalletAccountInfo(userId = userId) }
        runCatching { pushNotificationsTokenUpdater.updateTokenForAllUsers() }
        runCatching { mutedItemRepository.fetchAndPersistMuteList(userId = userId) }
        mutedItemRepository.fetchAndPersistStreamMuteList(userId = userId)

        return Result.success(Unit)
    }

    private suspend fun migrateWalletData(userAccount: UserAccount) {
        when (userAccount.walletPreference) {
            WalletPreference.NostrWalletConnect -> {
                userAccount.nostrWallet?.let { nwcData ->
                    val walletId = nwcData.keypair.privateKey.asSha256Hash()
                    walletRepository.upsertNostrWallet(
                        userId = userAccount.pubkey,
                        wallet = Wallet.NWC(
                            walletId = walletId,
                            userId = userAccount.pubkey,
                            lightningAddress = nwcData.lightningAddress,
                            spamThresholdAmountInSats = 0L,
                            balanceInBtc = null,
                            maxBalanceInBtc = null,
                            lastUpdatedAt = Clock.System.now().epochSeconds,
                            pubkey = nwcData.pubkey,
                            relays = nwcData.relays,
                            keypair = nwcData.keypair,
                        ),
                    )

                    walletAccountRepository.setActiveWallet(userId = userAccount.pubkey, walletId = walletId)
                }
            }

            WalletPreference.Undefined, WalletPreference.PrimalWallet -> {
                primalWalletAccountRepository.fetchWalletAccountInfo(userId = userAccount.pubkey)

                walletRepository.updateWalletBalance(
                    walletId = userAccount.pubkey,
                    balanceInBtc = userAccount.primalWalletState.balanceInBtc?.toDouble() ?: 0.0,
                    maxBalanceInBtc = userAccount.primalWalletSettings.maxBalanceInBtc.toDouble(),
                )

                walletRepository.upsertWalletSettings(
                    walletId = userAccount.pubkey,
                    spamThresholdAmountInSats = userAccount.primalWalletSettings.spamThresholdAmountInSats,
                )

                walletAccountRepository.setActiveWallet(userId = userAccount.pubkey, walletId = userAccount.pubkey)
            }
        }

        userRepository.clearWalletData(userId = userAccount.pubkey)
    }
}
