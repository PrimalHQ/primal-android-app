package net.primal.wallet.data.remote.api

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.wallet.data.remote.WalletOperationVerb
import net.primal.wallet.data.remote.model.WalletOperationRequestBody
import net.primal.wallet.data.remote.model.WalletRequestBody
import net.primal.wallet.data.remote.serialization.encodeToWalletJsonString

internal suspend fun buildWalletOptionsJson(
    userId: String,
    walletVerb: WalletOperationVerb,
    requestBody: WalletOperationRequestBody,
    signatureHandler: NostrEventSignatureHandler,
): String {
    val walletAuthorizationEvent = signatureHandler.signNostrEvent(
        unsignedNostrEvent = NostrUnsignedEvent(
            pubKey = userId,
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
            kind = NostrEventKind.PrimalWalletOperation.value,
            tags = listOf(),
        ),
    ).unwrapOrThrow()

    return WalletRequestBody(event = walletAuthorizationEvent).encodeToWalletJsonString()
}
