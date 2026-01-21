package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.createAppBuildHelper
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.PromoCodeDetails
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.wallet.WalletKycLevel
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.PrimalWalletData
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.PromoCodeRequestBody
import net.primal.wallet.data.repository.mappers.remote.toPromoCodeDetailsDO

class PrimalWalletAccountRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletApi: PrimalWalletApi,
    private val walletDatabase: WalletDatabase,
    private val signatureHandler: NostrEventSignatureHandler,
) : PrimalWalletAccountRepository {

    private val appBuildHelper = createAppBuildHelper()

    override suspend fun fetchWalletAccountInfo(userId: String): Result<String> =
        withContext(dispatcherProvider.io()) {
            runCatching {
                val accountInfoResponse = primalWalletApi.getWalletUserInfo(userId)

                val kycLevel = WalletKycLevel.Companion.valueOf(accountInfoResponse.kycLevel)
                    ?: throw IllegalArgumentException("Couldn't parse KycLevel.")

                val lightningAddress = accountInfoResponse.lightningAddress
                walletDatabase.withTransaction {
                    walletDatabase.wallet().insertOrIgnoreWalletInfo(
                        info = WalletInfo(
                            walletId = userId,
                            userId = userId.asEncryptable(),
                            lightningAddress = lightningAddress.asEncryptable(),
                            type = WalletType.PRIMAL,
                        ),
                    )

                    walletDatabase.wallet().updateWalletLightningAddress(
                        walletId = userId,
                        lightningAddress = lightningAddress.asEncryptable(),
                    )

                    walletDatabase.wallet().upsertPrimalWalletData(
                        data = PrimalWalletData(
                            walletId = userId,
                            kycLevel = kycLevel,
                        ),
                    )
                }

                userId
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
