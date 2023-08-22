package net.primal.android.wallet.repository

import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.repository.PostStatsUpdater
import net.primal.android.networking.relays.RelayPool
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.wallet.api.ZapsApi
import net.primal.android.wallet.model.ZapTarget
import java.io.IOException
import javax.inject.Inject

class ZapRepository @Inject constructor(
    private val zapsApi: ZapsApi,
    private val notary: NostrNotary,
    private val relayPool: RelayPool,
    private val accountsStore: UserAccountsStore,
    private val database: PrimalDatabase,
) {
    suspend fun zap(
        userId: String,
        comment: String,
        amount: Int,
        target: ZapTarget,
    ) {
        val userAccount = accountsStore.findByIdOrNull(pubkey = userId)
        val nostrWallet = userAccount?.nostrWallet
        val walletRelays = userAccount?.relays

        val lightningAddress = when (target) {
            is ZapTarget.Note -> target.authorLightningAddress
            is ZapTarget.Profile -> target.lightningAddress
        }

        if (lightningAddress.isEmpty() || nostrWallet == null || walletRelays.isNullOrEmpty()) {
            throw InvalidZapRequestException()
        }

        val zapEvent = notary.signZapRequestNostrEvent(
            userId = userId,
            comment = comment,
            target = target,
            relays = walletRelays,
        )
        val zapPayRequest = try {
            zapsApi.fetchZapPayRequest(lightningAddress)
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        } catch (error: IllegalArgumentException) {
            throw ZapFailureException(cause = error)
        }

        val invoice = try {
            zapsApi.fetchInvoice(
                request = zapPayRequest,
                zapEvent = zapEvent,
                satoshiAmount = amount * 1000,
                comment = comment
            )
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        }

        val walletPayNostrEvent = notary.signWalletInvoiceRequestNostrEvent(
            request = invoice.toWalletPayRequest(),
            nwc = nostrWallet
        )


        val statsUpdater = when (target) {
            is ZapTarget.Note -> PostStatsUpdater(
                postId = target.id,
                userId = userId,
                database = database
            )

            is ZapTarget.Profile -> null
        }
        try {
            statsUpdater?.increaseZapStats(satsAmount = amount)
            relayPool.publishEvent(nostrEvent = walletPayNostrEvent)
        } catch (error: NostrPublishException) {
            statsUpdater?.revertStats()
            throw error
        }

    }

    data class ZapFailureException(override val cause: Throwable) : RuntimeException()

    data class InvalidZapRequestException(override val cause: Throwable? = null) : IllegalArgumentException()
}
