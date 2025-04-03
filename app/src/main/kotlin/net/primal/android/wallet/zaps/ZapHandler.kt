package net.primal.android.wallet.zaps

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.networking.relays.FALLBACK_RELAYS
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.domain.WalletPreference
import net.primal.android.user.domain.mapToRelayDO
import net.primal.android.user.repository.RelayRepository
import net.primal.android.wallet.domain.WalletKycLevel
import net.primal.android.wallet.domain.ZapTarget
import net.primal.android.wallet.domain.lnUrlDecoded
import net.primal.android.wallet.domain.userId
import net.primal.android.wallet.nwc.NwcNostrZapperFactory
import net.primal.android.wallet.repository.WalletNostrZapper
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.repository.events.EventStatsUpdater

class ZapHandler @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val accountsStore: UserAccountsStore,
    private val nwcNostrZapperFactory: NwcNostrZapperFactory,
    private val primalWalletZapper: WalletNostrZapper,
    private val relayRepository: RelayRepository,
    private val notary: NostrNotary,
) {

    @Throws(ZapFailureException::class)
    suspend fun zap(
        userId: String,
        target: ZapTarget,
        amountInSats: ULong? = null,
        comment: String? = null,
    ) = withContext(dispatcherProvider.io()) {
        val userAccount = accountsStore.findByIdOrNull(userId = userId)
        val userRelays = relayRepository.findRelays(userId, RelayKind.UserRelay)
            .map { it.mapToRelayDO() }
            .ifEmpty { FALLBACK_RELAYS }
        val targetLnUrlDecoded = target.lnUrlDecoded()
        val nostrZapper = userAccount?.resolveZapper()
        val defaultZapOptions = userAccount?.appSettings?.zapDefault
        val zapAmountInSats = amountInSats ?: defaultZapOptions?.amount?.toULong()
        val zapComment = comment ?: defaultZapOptions?.message ?: ""

        if (nostrZapper == null || zapAmountInSats == null) throw InvalidZapRequestException()

        val statsUpdater = target.buildPostStatsUpdaterIfApplicable(userId)

        try {
            statsUpdater?.increaseZapStats(
                amountInSats = zapAmountInSats.toInt(),
                zapComment = zapComment,
            )

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

    private fun UserAccount.resolveZapper(): NostrZapper? {
        return when (this.walletPreference) {
            WalletPreference.NostrWalletConnect -> {
                if (this.nostrWallet != null) {
                    nwcNostrZapperFactory.create(nwcData = this.nostrWallet)
                } else {
                    null
                }
            }

            else -> {
                if (this.primalWallet != null && this.primalWallet.kycLevel != WalletKycLevel.None) {
                    primalWalletZapper
                } else {
                    null
                }
            }
        }
    }

    private fun ZapTarget.buildPostStatsUpdaterIfApplicable(userId: String): EventStatsUpdater? =
        // TODO Refactor EventStatsUpdater() to work without database
        null
//        when (this) {
//            is ZapTarget.Event -> EventStatsUpdater(
//                userId = userId,
//                eventId = this.eventId,
//                eventAuthorId = this.eventAuthorId,
//                database = database,
//            )
//
//            is ZapTarget.ReplaceableEvent -> EventStatsUpdater(
//                userId = userId,
//                eventId = this.eventId,
//                eventAuthorId = this.eventAuthorId,
//                database = database,
//            )
//
//            is ZapTarget.Profile -> null
//        }
}
