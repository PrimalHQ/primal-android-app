package net.primal.core.networking.nwc

import fr.acinq.secp256k1.Hex
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonObject
import net.primal.core.networking.nwc.model.NwcWalletRequest
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.CryptoUtils

class NwcApiImpl(
    private val nwcSocketUrl: String,
) : NwcApi {

    override suspend fun zap() {
        TODO("Not yet implemented")
    }

    override suspend fun getBalance(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactions(): List<JsonObject> {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun signWalletBalanceRequestNostrEvent(request: NwcWalletRequest<Unit>, nwc: NostrWalletConnect): SignResult {
        val tags = listOf(nwc.pubkey.asPubkeyTag())

        val plaintext = CommonJson.encodeToString(
            NwcWalletRequest.serializer(Unit.serializer()),
            request,
        )

        val encrypted = CryptoUtils.encrypt(
            msg = plaintext,
            privateKey = Hex.decode(nwc.keypair.privateKey),
            pubKey = Hex.decode(nwc.pubkey),
        )

        val unsigned = NostrUnsignedEvent(
            pubKey = nwc.keypair.pubkey,
            kind = NostrEventKind.WalletRequest.value,
            content = encrypted,
            tags = tags,
        )

        return runCatching {
            SignResult.Signed(
                unsigned.signOrThrow(hexPrivateKey = Hex.decode(nwc.keypair.privateKey)),
            )
        }.getOrElse { SignResult.Rejected(SignatureException(cause = it)) }
    }
}
