package net.primal.android.wallet.api

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.primal.android.core.serialization.json.NostrJsonEncodeDefaults
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.wallet.api.model.WalletOperationRequestBody
import net.primal.android.wallet.api.model.WalletOperationVerb
import net.primal.android.wallet.api.model.WalletRequestBody
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow

suspend fun buildWalletOptionsJson(
    userId: String,
    walletVerb: WalletOperationVerb,
    requestBody: WalletOperationRequestBody,
    nostrNotary: NostrNotary,
): String =
    NostrJsonEncodeDefaults.encodeToString(
        WalletRequestBody(
            event = nostrNotary.signPrimalWalletOperationNostrEvent(
                userId = userId,
                content = buildJsonArray {
                    add(walletVerb.identifier)
                    add(
                        CommonJson.encodeToJsonElement(requestBody).let {
                            val map = it.jsonObject.toMutableMap()
                            map.remove("type")
                            JsonObject(map)
                        },
                    )
                }.toString(),
            ).unwrapOrThrow(),
        ),
    )
