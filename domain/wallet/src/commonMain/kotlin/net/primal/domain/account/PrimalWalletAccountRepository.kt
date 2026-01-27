package net.primal.domain.account

import kotlin.coroutines.cancellation.CancellationException
import net.primal.core.utils.Result
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException

interface PrimalWalletAccountRepository {

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun fetchWalletAccountInfo(userId: String): Result<String>

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
