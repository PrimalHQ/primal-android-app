package net.primal.domain.account

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.wallet.Wallet

interface WalletAccountRepository {
    suspend fun setActiveWallet(userId: String, walletId: String)

    suspend fun clearActiveWallet(userId: String)

    suspend fun findLastUsedNostrWallet(userId: String): Wallet?

    fun observeActiveWallet(userId: String): Flow<Wallet?>

    fun observeActiveWalletId(userId: String): Flow<String?>

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun activateWallet(userId: String, code: String): WalletActivationResult

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun requestActivationCodeToEmail(params: WalletActivationParams)

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun fetchWalletAccountInfo(userId: String)

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun getPromoCodeDetails(code: String): PromoCodeDetails

    @Throws(
        SignatureException::class,
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun redeemPromoCode(userId: String, code: String)
}
