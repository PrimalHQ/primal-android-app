package net.primal.core.networking.nwc.wallet

import fr.acinq.secp256k1.Hex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import net.primal.core.networking.nwc.wallet.model.NwcRequestUnauthenticatedException
import net.primal.core.networking.nwc.wallet.model.WalletNwcRequest
import net.primal.core.nips.encryption.service.NostrEncryptionService
import net.primal.core.utils.Result
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.cryptography.NostrKeyPair
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.cryptography.utils.CryptoUtils

class NwcWalletRequestParserTest {

    private val clientKeyPair = CryptoUtils.generateHexEncodedKeypair()
    private val serviceKeyPair = CryptoUtils.generateHexEncodedKeypair()

    private val payInvoiceJson =
        """{"method":"pay_invoice","params":{"invoice":"lnbc10u1pjq0000attackercontrolledinvoice"}}"""

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

    private class RecordingEncryptionService(private val plaintext: String) : NostrEncryptionService {
        var decryptionAttempts = 0
            private set

        override fun nip04Encrypt(
            privateKey: String,
            pubKey: String,
            plaintext: String,
        ) = Result.success(plaintext)

        override fun nip44Encrypt(
            privateKey: String,
            pubKey: String,
            plaintext: String,
        ) = Result.success(plaintext)

        override fun nip04Decrypt(
            privateKey: String,
            pubKey: String,
            ciphertext: String,
        ): Result<String> {
            decryptionAttempts++
            return Result.success(plaintext)
        }

        override fun nip44Decrypt(
            privateKey: String,
            pubKey: String,
            ciphertext: String,
        ): Result<String> {
            decryptionAttempts++
            return Result.success(plaintext)
        }
    }

    private fun buildRequestEvent(signedBy: NostrKeyPair): NostrEvent =
        NostrUnsignedEvent(
            pubKey = signedBy.pubKey,
            createdAt = 1_700_000_000L,
            kind = NostrEventKind.NwcRequest.value,
            tags = listOf(serviceKeyPair.pubKey.asPubkeyTag()),
            content = "ciphertext-irrelevant-to-authentication",
        ).signOrThrow(hexPrivateKey = Hex.decode(signedBy.privateKey))

    @Test
    fun parseNostrEvent_rejectsPayInvoiceFromAnUnauthorizedAuthor() {
        val encryptionService = RecordingEncryptionService(plaintext = payInvoiceJson)
        val parser = NwcWalletRequestParser(
            encryptionService = encryptionService,
            requestAuthenticator = NwcRequestAuthenticator(),
        )
        val attackerKeyPair = CryptoUtils.generateHexEncodedKeypair()

        val result = parser.parseNostrEvent(
            event = buildRequestEvent(signedBy = attackerKeyPair),
            connection = connection,
        )

        assertTrue(result.isFailure, "A pay_invoice from an unauthorized author must not be parsed.")
        assertIs<NwcRequestUnauthenticatedException>(result.exceptionOrNull())
        assertEquals(
            expected = 0,
            actual = encryptionService.decryptionAttempts,
            message = "Unauthenticated events must be rejected before any decryption is attempted.",
        )
    }

    @Test
    fun parseNostrEvent_acceptsPayInvoiceFromTheAuthorizedClient() {
        val encryptionService = RecordingEncryptionService(plaintext = payInvoiceJson)
        val parser = NwcWalletRequestParser(
            encryptionService = encryptionService,
            requestAuthenticator = NwcRequestAuthenticator(),
        )

        val result = parser.parseNostrEvent(
            event = buildRequestEvent(signedBy = clientKeyPair),
            connection = connection,
        )

        assertTrue(result.isSuccess, "A validly signed request from the authorized client must be parsed.")
        assertIs<WalletNwcRequest.PayInvoice>(result.getOrNull())
    }
}
