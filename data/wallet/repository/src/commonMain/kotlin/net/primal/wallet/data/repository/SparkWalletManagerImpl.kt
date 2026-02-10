package net.primal.wallet.data.repository

import breez_sdk_spark.EventListener
import breez_sdk_spark.SdkEvent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.UnclaimedDeposit
import net.primal.domain.wallet.UnclaimedDepositEvent
import net.primal.wallet.data.spark.BreezSdkInstanceManager
import net.primal.wallet.data.validator.RecoveryPhraseValidator

internal class SparkWalletManagerImpl(
    private val breezSdkInstanceManager: BreezSdkInstanceManager,
) : SparkWalletManager {

    private val recoveryPhraseValidator = RecoveryPhraseValidator()

    private val mutex = Mutex()
    private val eventListenerIds = mutableMapOf<String, String>()

    private val _unclaimedDeposits = MutableSharedFlow<UnclaimedDepositEvent>()
    override val unclaimedDeposits: Flow<UnclaimedDepositEvent> = _unclaimedDeposits.asSharedFlow()

    override suspend fun initializeWallet(seedWords: String): Result<String> =
        runCatching {
            if (!recoveryPhraseValidator.isValid(seedWords)) {
                error("Invalid recovery phrase: expected 12, 15, 18, 21, or 24 valid BIP39 words.")
            }
            mutex.withLock {
                val walletId = breezSdkInstanceManager.initWallet(seedWords)
                if (walletId !in eventListenerIds) {
                    val sdk = breezSdkInstanceManager.requireInstance(walletId)
                    eventListenerIds[walletId] = sdk.addEventListener(
                        SdkEventListener(walletId = walletId),
                    )
                }
                walletId
            }
        }

    override suspend fun disconnectWallet(walletId: String): Result<Unit> =
        runCatching {
            mutex.withLock {
                eventListenerIds.remove(walletId)?.let { listenerId ->
                    breezSdkInstanceManager.getInstance(walletId)?.removeEventListener(listenerId)
                }
                breezSdkInstanceManager.removeInstance(walletId)
            }
        }

    private inner class SdkEventListener(
        private val walletId: String,
    ) : EventListener {
        override suspend fun onEvent(event: SdkEvent) {
            when (event) {
                is SdkEvent.Synced ->
                    Napier.d { "SdkEvent.Synced walletId=$walletId" }

                is SdkEvent.PaymentSucceeded ->
                    Napier.i { "SdkEvent.PaymentSucceeded walletId=$walletId paymentId=${event.payment.id}" }

                is SdkEvent.PaymentPending ->
                    Napier.i { "SdkEvent.PaymentPending walletId=$walletId paymentId=${event.payment.id}" }

                is SdkEvent.PaymentFailed ->
                    Napier.w { "SdkEvent.PaymentFailed walletId=$walletId paymentId=${event.payment.id}" }

                is SdkEvent.UnclaimedDeposits -> {
                    val deposits = event.unclaimedDeposits
                    Napier.i { "SdkEvent.UnclaimedDeposits walletId=$walletId count=${deposits.size}" }
                    _unclaimedDeposits.emit(
                        UnclaimedDepositEvent(
                            walletId = walletId,
                            deposits = deposits.map { UnclaimedDeposit(txid = it.txid, amountSats = it.amountSats.toLong()) },
                        ),
                    )
                }

                is SdkEvent.ClaimedDeposits ->
                    Napier.i { "SdkEvent.ClaimedDeposits walletId=$walletId" }

                is SdkEvent.Optimization ->
                    Napier.d { "SdkEvent.Optimization walletId=$walletId" }
            }
        }
    }
}
