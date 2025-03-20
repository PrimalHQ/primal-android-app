package net.primal.android.wallet.api

import javax.inject.Inject
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.wallet.api.model.EmptyRequestBody
import net.primal.android.wallet.api.model.NwcConnectionCreatedResponse
import net.primal.android.wallet.api.model.NwcCreateNewConnectionRequestBody
import net.primal.android.wallet.api.model.NwcRevokeConnectionRequestBody
import net.primal.android.wallet.api.model.PrimalNwcConnectionInfo
import net.primal.android.wallet.api.model.WalletOperationVerb
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.domain.nostr.NostrEventKind

class NwcPrimalWalletApiImpl @Inject constructor(
    @PrimalWalletApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : NwcPrimalWalletApi {

    override suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.NWC_CONNECTIONS,
                    requestBody = EmptyRequestBody,
                    nostrNotary = nostrNotary,
                ),
            ),
        )
        val info = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletNwcConnectionList)
            ?: throw WssException("Event with kind 10000321 not found.")

        return CommonJson.decodeFromStringOrNull<List<PrimalNwcConnectionInfo>>(info.content)
            ?: throw WssException("Invalid event with kind 10000321.")
    }

    override suspend fun revokeConnection(userId: String, nwcPubkey: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = net.primal.data.remote.PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.NWC_REVOKE_CONNECTION,
                    requestBody = NwcRevokeConnectionRequestBody(nwcPubKey = nwcPubkey),
                    nostrNotary = nostrNotary,
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
                primalVerb = net.primal.data.remote.PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.NWC_CREATE_NEW_CONNECTION,
                    requestBody = NwcCreateNewConnectionRequestBody(appName = appName, dailyBudgetBtc = dailyBudgetBtc),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        val info = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletNwcConnectionCreated)
            ?: throw WssException("Event with kind 10000319 not found.")

        return CommonJson.decodeFromStringOrNull<NwcConnectionCreatedResponse>(info.content)
            ?: throw WssException("Invalid event with kind 10000319.")
    }
}
