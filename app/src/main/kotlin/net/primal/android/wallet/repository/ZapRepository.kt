package net.primal.android.wallet.repository

import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.repository.PostStatsUpdater
import net.primal.android.networking.relays.RelayPool
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.wallet.api.ZapsApi
import net.primal.android.wallet.model.LightningPayRequest
import net.primal.android.wallet.model.LightningPayResponse
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
        target: ZapTarget,
        amountInSats: ULong? = null,
        comment: String? = null,
    ) {
        val userAccount = accountsStore.findByIdOrNull(userId = userId)
        val nostrWallet = userAccount?.nostrWallet
        val walletRelays = userAccount?.relays
        val defaultZapAmount = userAccount?.appSettings?.defaultZapAmount

        val lightningAddress = when (target) {
            is ZapTarget.Note -> target.authorLightningAddress
            is ZapTarget.Profile -> target.lightningAddress
        }

        if (lightningAddress.isEmpty() || nostrWallet == null || walletRelays.isNullOrEmpty()) {
            throw InvalidZapRequestException()
        }

        val statsUpdater = when (target) {
            is ZapTarget.Note -> PostStatsUpdater(
                postId = target.id,
                userId = userId,
                database = database,
            )

            is ZapTarget.Profile -> null
        }

        val zapAmountInSats = amountInSats ?: defaultZapAmount ?: 42L.toULong()
        val zapComment = comment ?: ""

        try {
            statsUpdater?.increaseZapStats(amountInSats = zapAmountInSats.toInt())
            val zapPayRequest = zapsApi.fetchZapPayRequestOrThrow(lightningAddress)
            val zapEvent = notary.signZapRequestNostrEvent(
                userId = userId,
                comment = zapComment,
                target = target,
                relays = walletRelays,
            )
            val invoice = zapsApi.fetchInvoiceOrThrow(
                zapPayRequest = zapPayRequest,
                zapEvent = zapEvent,
                satoshiAmountInMilliSats = zapAmountInSats * 1000.toULong(),
                comment = zapComment,
            )
            val walletPayNostrEvent = notary.signWalletInvoiceRequestNostrEvent(
                request = invoice.toWalletPayRequest(),
                nwc = nostrWallet,
            )
            relayPool.publishEvent(nostrEvent = walletPayNostrEvent)
        } catch (error: ZapFailureException) {
            statsUpdater?.revertStats()
            throw error
        } catch (error: NostrPublishException) {
            statsUpdater?.revertStats()
            throw error
        }
    }

    private suspend fun ZapsApi.fetchZapPayRequestOrThrow(lightningAddress: String): LightningPayRequest {
        return try {
            fetchZapPayRequest(lightningAddress)
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        } catch (error: IllegalArgumentException) {
            throw ZapFailureException(cause = error)
        }
    }

    private suspend fun ZapsApi.fetchInvoiceOrThrow(
        zapPayRequest: LightningPayRequest,
        zapEvent: NostrEvent,
        satoshiAmountInMilliSats: ULong,
        comment: String = ""
    ): LightningPayResponse {
        return try {
            this.fetchInvoice(
                request = zapPayRequest,
                zapEvent = zapEvent,
                satoshiAmountInMilliSats = satoshiAmountInMilliSats,
                comment = comment
            )
        } catch (error: IOException) {
            throw ZapFailureException(cause = error)
        }
    }

    data class ZapFailureException(override val cause: Throwable) : RuntimeException()

    data class InvalidZapRequestException(override val cause: Throwable? = null) :
        IllegalArgumentException()
}
