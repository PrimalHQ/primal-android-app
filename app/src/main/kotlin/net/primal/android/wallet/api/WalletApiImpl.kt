package net.primal.android.wallet.api

import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.asWalletBalanceInBtcOrNull
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.WalletActivationContent
import net.primal.android.nostr.model.primal.content.WalletUserInfoContent
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.wallet.api.model.ActivateWalletRequestBody
import net.primal.android.wallet.api.model.BalanceRequestBody
import net.primal.android.wallet.api.model.DepositRequestBody
import net.primal.android.wallet.api.model.GetActivationCodeRequestBody
import net.primal.android.wallet.api.model.IsWalletUserRequestBody
import net.primal.android.wallet.api.model.TransactionsRequestBody
import net.primal.android.wallet.api.model.UserWalletInfoRequestBody
import net.primal.android.wallet.api.model.WalletOperationRequestBody
import net.primal.android.wallet.api.model.WalletOperationVerb
import net.primal.android.wallet.api.model.WalletRequestBody
import net.primal.android.wallet.api.model.WalletUserInfoResponse
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.SubWallet

class WalletApiImpl @Inject constructor(
    @PrimalWalletApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : WalletApi {

    override suspend fun getWalletUserKycLevel(userId: String): Int {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.IS_USER,
                    requestBody = IsWalletUserRequestBody(userId),
                ),
            ),
        )

        val isUserEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletIsUser)
        return isUserEvent?.content?.toIntOrNull() ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun getWalletUserInfo(userId: String): WalletUserInfoResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.USER_INFO,
                    requestBody = UserWalletInfoRequestBody(userId),
                ),
            ),
        )

        return queryResult
            .findPrimalEvent(NostrEventKind.PrimalWalletUserInfo)
            .toUserWalletInfoResponseOrThrow()
    }

    override suspend fun requestActivationCodeToEmail(
        userId: String,
        name: String,
        email: String,
    ) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.GET_ACTIVATION_CODE,
                    requestBody = GetActivationCodeRequestBody(name, email),
                ),
            ),
        )
    }

    override suspend fun activateWallet(userId: String, code: String): String {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.ACTIVATE,
                    requestBody = ActivateWalletRequestBody(code),
                ),
            ),
        )

        return queryResult
            .findPrimalEvent(NostrEventKind.PrimalWalletActivation)
            .toWalletLightningAddressOrThrow()
    }

    override suspend fun balance(userId: String): Double {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.BALANCE,
                    requestBody = BalanceRequestBody(subWallet = SubWallet.Open),
                ),
            ),
        )

        return queryResult.findPrimalEvent(NostrEventKind.PrimalWalletBalance)
            ?.asWalletBalanceInBtcOrNull()
            ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun withdraw(userId: String, body: WithdrawRequestBody) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.WITHDRAW,
                    requestBody = body,
                ),
            ),
        )
    }

    override suspend fun deposit(userId: String, body: DepositRequestBody) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.DEPOSIT,
                    requestBody = body,
                ),
            ),
        )
    }

    override suspend fun transactions(userId: String, body: TransactionsRequestBody) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.TRANSACTIONS,
                    requestBody = body,
                ),
            ),
        )
    }

    private fun buildWalletOptionsJson(
        userId: String,
        walletVerb: WalletOperationVerb,
        requestBody: WalletOperationRequestBody,
    ): String =
        NostrJsonEncodeDefaults.encodeToString(
            WalletRequestBody(
                event = nostrNotary.signPrimalWalletOperationNostrEvent(
                    userId = userId,
                    content = buildJsonArray {
                        add(walletVerb.identifier)
                        add(
                            NostrJson.encodeToJsonElement(requestBody).let {
                                val map = it.jsonObject.toMutableMap()
                                map.remove("type")
                                JsonObject(map)
                            },
                        )
                    }.toString(),
                ),
            ),
        )

    private fun PrimalEvent?.toUserWalletInfoResponseOrThrow(): WalletUserInfoResponse {
        val content = takeContentOrNull<WalletUserInfoContent>()
            ?: throw WssException("Missing or invalid content in response.")
        return WalletUserInfoResponse(
            kycLevel = content.kycLevel,
            lightningAddress = content.lud16,
        )
    }

    private fun PrimalEvent?.toWalletLightningAddressOrThrow(): String {
        val content = takeContentOrNull<WalletActivationContent>()
            ?: throw WssException("Missing or invalid content in response.")
        return content.lud16
    }
}
