package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.createAppBuildHelper
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.PromoCodeDetails
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.account.WalletActivationParams
import net.primal.domain.account.WalletActivationResult
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.db.withTransaction
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.PromoCodeRequestBody
import net.primal.wallet.data.repository.mappers.local.toWalletActivationRequestDTO
import net.primal.wallet.data.repository.mappers.remote.toPromoCodeDetailsDO

class WalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletApi: PrimalWalletApi,
    private val walletDatabase: WalletDatabase,
    private val signatureHandler: NostrEventSignatureHandler,
) : WalletAccountRepository {

    private val appBuildHelper = createAppBuildHelper()

    override suspend fun activateWallet(userId: String, code: String): WalletActivationResult {
        val response = primalWalletApi.activateWallet(userId, code)
        return WalletActivationResult(lightningAddress = response)
    }

    override suspend fun requestActivationCodeToEmail(params: WalletActivationParams) {
        withContext(dispatcherProvider.io()) {
            primalWalletApi.requestActivationCodeToEmail(
                userId = params.userId,
                body = params.toWalletActivationRequestDTO(),
            )
        }
    }

    override suspend fun fetchWalletAccountInfo(userId: String) {
        val accountInfoResponse = primalWalletApi.getWalletUserInfo(userId)
        val kycLevel = WalletKycLevel.Companion.valueOf(accountInfoResponse.kycLevel) ?: return
        val lightningAddress = accountInfoResponse.lightningAddress
        walletDatabase.withTransaction {
            val existingPrimalWallet = walletDatabase.wallet().findWalletInfo(userId = userId, type = WalletType.PRIMAL)
            val updatedInfo = existingPrimalWallet?.copy(
                userId = userId,
                kycLevel = kycLevel,
                lightningAddress = lightningAddress,
            ) ?: WalletInfo(
                userId = userId,
                kycLevel = kycLevel,
                lightningAddress = lightningAddress,
                type = WalletType.PRIMAL,
            )
            walletDatabase.wallet().upsertWalletInfo(updatedInfo)
        }
    }

    override suspend fun getPromoCodeDetails(code: String): PromoCodeDetails =
        withContext(dispatcherProvider.io()) {
            val response = primalWalletApi.getPromoCodeDetails(code = code)
            response.toPromoCodeDetailsDO()
        }

    override suspend fun redeemPromoCode(userId: String, code: String) =
        withContext(dispatcherProvider.io()) {
            val authorization = signatureHandler.signNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.ApplicationSpecificData.value,
                    tags = listOf("${appBuildHelper.getAppName()} App".asIdentifierTag()),
                    content = PromoCodeRequestBody(promoCode = code).encodeToJsonString(),
                ),
            ).unwrapOrThrow()

            primalWalletApi.redeemPromoCode(authorizationEvent = authorization)
        }
}
