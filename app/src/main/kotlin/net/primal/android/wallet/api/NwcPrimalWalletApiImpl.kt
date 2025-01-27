package net.primal.android.wallet.api

import javax.inject.Inject
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.wallet.api.model.EmptyRequestBody
import net.primal.android.wallet.api.model.NwcRevokeConnectionRequestBody
import net.primal.android.wallet.api.model.PrimalNwcConnectionInfo
import net.primal.android.wallet.api.model.WalletOperationVerb
import timber.log.Timber

class NwcPrimalWalletApiImpl @Inject constructor(
    @PrimalWalletApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : NwcPrimalWalletApi {

    override suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo> {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.NWC_CONNECTIONS,
                    requestBody = EmptyRequestBody,
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        Timber.i("Here: $queryResult")
        val info = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletNwcConnectionList)
            ?: throw WssException("Event with kind 10000321 not found.")

        return NostrJson.decodeFromStringOrNull<List<PrimalNwcConnectionInfo>>(info.content)
            ?: throw WssException("Invalid event with kind 10000321.")
    }

    override suspend fun revokeConnection(userId: String, nwcPubkey: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.NWC_REVOKE_CONNECTION,
                    requestBody = NwcRevokeConnectionRequestBody(nwcPubKey = nwcPubkey),
                    nostrNotary = nostrNotary,
                ),
            ),
        )
    }
}
