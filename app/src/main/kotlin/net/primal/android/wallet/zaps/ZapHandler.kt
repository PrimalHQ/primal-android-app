package net.primal.android.wallet.zaps

import javax.inject.Inject
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.repository.PostStatsUpdater
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.domain.WalletKycLevel
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.domain.lnUrlDecoded
import net.primal.android.wallet.domain.userId
import net.primal.android.wallet.nwc.NwcNostrZapper
import net.primal.android.wallet.nwc.api.NwcApi
import net.primal.android.wallet.repository.WalletNostrZapper
import net.primal.android.wallet.repository.WalletRepository

class ZapHandler @Inject constructor(
    private val accountsStore: UserAccountsStore,
    private val notary: NostrNotary,
    private val relaysManager: RelaysManager,
    private val walletRepository: WalletRepository,
    private val nwcApi: NwcApi,
    private val database: PrimalDatabase,
) {

    @Throws(ZapFailureException::class)
    suspend fun zap(
        userId: String,
        target: ZapTarget,
        amountInSats: ULong? = null,
        comment: String? = null,
    ) {
        val userAccount = accountsStore.findByIdOrNull(userId = userId)
        val userRelays = userAccount?.relays
        val targetLnUrlDecoded = target.lnUrlDecoded()
        val nostrZapper = userAccount?.buildZapper()
        val defaultZapOptions = userAccount?.appSettings?.zapDefault
        val zapAmountInSats = amountInSats ?: defaultZapOptions?.amount?.toULong()
        val zapComment = comment ?: defaultZapOptions?.message ?: ""

        if (userRelays.isNullOrEmpty() || nostrZapper == null || zapAmountInSats == null) {
            throw InvalidZapRequestException()
        }

        val statsUpdater = target.buildPostStatsUpdaterIfApplicable(userId)

        try {
            statsUpdater?.increaseZapStats(amountInSats = zapAmountInSats.toInt())
            val userZapRequestEvent = notary.signZapRequestNostrEvent(
                userId = userId,
                comment = zapComment,
                target = target,
                relays = userRelays,
            )

            nostrZapper.zap(
                data = ZapRequestData(
                    zapperUserId = userId,
                    targetUserId = target.userId(),
                    lnUrlDecoded = targetLnUrlDecoded,
                    zapAmountInSats = zapAmountInSats,
                    zapComment = zapComment,
                    userZapRequestEvent = userZapRequestEvent,
                ),
            )
        } catch (error: ZapFailureException) {
            statsUpdater?.revertStats()
            throw error
        }
    }

    private fun UserAccount.buildZapper(): NostrZapper? {
        return when (this.walletPreference) {
            WalletPreference.NostrWalletConnect -> {
                if (this.nostrWallet != null) {
                    NwcNostrZapper(
                        relaysManager = relaysManager,
                        notary = notary,
                        nwcApi = nwcApi,
                        nostrWallet = this.nostrWallet,
                    )
                } else {
                    null
                }
            }

            else -> {
                if (this.primalWallet != null && this.primalWallet.kycLevel != WalletKycLevel.None) {
                    WalletNostrZapper(
                        walletRepository = walletRepository,
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun ZapTarget.buildPostStatsUpdaterIfApplicable(userId: String) =
        when (this) {
            is ZapTarget.Note -> PostStatsUpdater(
                postId = this.id,
                userId = userId,
                database = database,
            )

            is ZapTarget.Profile -> null
        }
}
