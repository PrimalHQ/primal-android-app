package net.primal.wallet.data.remote.api

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.wallet.data.remote.PrimalWalletVerb
import net.primal.wallet.data.remote.WalletOperationVerb
import net.primal.wallet.data.remote.model.EmptyRequestBody
import net.primal.wallet.data.remote.model.NwcConnectionCreatedResponse
import net.primal.wallet.data.remote.model.NwcCreateNewConnectionRequestBody
import net.primal.wallet.data.remote.model.NwcRevokeConnectionRequestBody
import net.primal.wallet.data.remote.model.PrimalNwcConnectionInfo

class PrimalWalletNwcApiImpl(
    private val primalApiClient: PrimalApiClient,
    private val signatureHandler: NostrEventSignatureHandler,
) : PrimalWalletNwcApi {

    override suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.NWC_CONNECTIONS,
                    requestBody = EmptyRequestBody,
                    signatureHandler = signatureHandler,
                ),
            ),
        )
        val info = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletNwcConnectionList)
            ?: throw NetworkException("Event with kind 10000321 not found.")

        return info.content.decodeFromJsonStringOrNull<List<PrimalNwcConnectionInfo>>()
            ?: throw NetworkException("Invalid event with kind 10000321.")
    }

    override suspend fun revokeConnection(userId: String, nwcPubkey: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.NWC_REVOKE_CONNECTION,
                    requestBody = NwcRevokeConnectionRequestBody(nwcPubKey = nwcPubkey),
                    signatureHandler = signatureHandler,
                ),
            ),
        )
    }

    override suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudgetBtc: String?,
    ): NwcConnectionCreatedResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.NWC_CREATE_NEW_CONNECTION,
                    requestBody = NwcCreateNewConnectionRequestBody(appName = appName, dailyBudgetBtc = dailyBudgetBtc),
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        val info = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletNwcConnectionCreated)
            ?: throw NetworkException("Event with kind 10000319 not found.")

        return info.content.decodeFromJsonStringOrNull<NwcConnectionCreatedResponse>()
            ?: throw NetworkException("Invalid event with kind 10000319.")
    }
}
