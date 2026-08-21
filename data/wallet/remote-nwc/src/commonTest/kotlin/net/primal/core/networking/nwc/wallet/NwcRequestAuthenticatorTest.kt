package net.primal.core.networking.nwc.wallet

import fr.acinq.secp256k1.Hex
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.toHex

class NwcRequestAuthenticatorTest {

    private val authenticator = NwcRequestAuthenticator()

    private val clientKeyPair = CryptoUtils.generateHexEncodedKeypair()
    private val serviceKeyPair = CryptoUtils.generateHexEncodedKeypair()

    private val connection = NwcConnection(
        walletId = "wallet-id",
        userId = "user-id",
        secretPubKey = clientKeyPair.pubKey,
        serviceKeyPair = NostrKeyPair(
            privateKey = serviceKeyPair.privateKey,
            pubKey = serviceKeyPair.pubKey,
        ),
        relay = "wss://relay.primal.net",
        appName = "Test App",
        dailyBudgetSats = 10_000L,
    )

    private fun buildSignedRequest(signedBy: NostrKeyPair, authoredBy: String = signedBy.pubKey): NostrEvent =
        NostrUnsignedEvent(
            pubKey = authoredBy,
            createdAt = 1_700_000_000L,
            kind = NostrEventKind.NwcRequest.value,
            tags = listOf(serviceKeyPair.pubKey.asPubkeyTag()),
            content = "encrypted-pay-invoice-payload",
        ).signOrThrow(hexPrivateKey = Hex.decode(signedBy.privateKey))

    @Test
    fun authenticate_rejectsRequestFromAnUnrelatedKeyPair() {
        val attackerKeyPair = CryptoUtils.generateHexEncodedKeypair()

        val event = buildSignedRequest(signedBy = attackerKeyPair)

        assertFalse(
            actual = authenticator.authenticate(event = event, connection = connection),
            message = "A validly signed event from a non-authorized author must be rejected.",
        )
    }

    @Test
    fun authenticate_acceptsRequestFromTheAuthorizedClient() {
        val event = buildSignedRequest(signedBy = clientKeyPair)

        assertTrue(
            actual = authenticator.authenticate(event = event, connection = connection),
            message = "A validly signed event from the authorized client must be accepted.",
        )
    }

    @Test
    fun authenticate_rejectsRequestImpersonatingTheClientPubKey() {
        val attackerKeyPair = CryptoUtils.generateHexEncodedKeypair()

        val event = buildSignedRequest(signedBy = attackerKeyPair, authoredBy = clientKeyPair.pubKey)

        assertFalse(
            actual = authenticator.authenticate(event = event, connection = connection),
            message = "Claiming the client pubkey without its private key must fail signature verification.",
        )
    }

    @Test
    fun authenticate_rejectsRequestWithTamperedSignature() {
        val event = buildSignedRequest(signedBy = clientKeyPair)
        val tampered = event.copy(sig = event.sig.replaceFirstChar { if (it == 'a') 'b' else 'a' })

        assertFalse(
            actual = authenticator.authenticate(event = tampered, connection = connection),
            message = "A tampered signature must be rejected.",
        )
    }

    @Test
    fun authenticate_rejectsRequestWhoseIdDoesNotMatchItsSignature() {
        val event = buildSignedRequest(signedBy = clientKeyPair)
        val tampered = event.copy(id = CryptoUtils.sha256("different-id".encodeToByteArray()).toHex())

        assertFalse(
            actual = authenticator.authenticate(event = tampered, connection = connection),
            message = "A signature that does not cover the presented event id must be rejected.",
        )
    }

    @Test
    fun authenticate_rejectsMalformedHexWithoutThrowing() {
        val event = buildSignedRequest(signedBy = clientKeyPair)

        assertFalse(authenticator.authenticate(event = event.copy(sig = "not-hex"), connection = connection))
        assertFalse(authenticator.authenticate(event = event.copy(sig = ""), connection = connection))
        assertFalse(
            actual = authenticator.authenticate(
                event = event.copy(id = "zz", pubKey = connection.secretPubKey),
                connection = connection,
            ),
        )
    }
}
