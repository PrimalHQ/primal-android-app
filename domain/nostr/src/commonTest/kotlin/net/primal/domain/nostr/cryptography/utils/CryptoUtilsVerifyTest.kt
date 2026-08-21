package net.primal.domain.nostr.cryptography.utils

import fr.acinq.secp256k1.Hex
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CryptoUtilsVerifyTest {

    private fun hashOf(text: String) = CryptoUtils.sha256(text.toByteArray())

    @Test
    fun verify_returnsTrue_forSignatureProducedByMatchingKeyPair() {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val hash = hashOf("pay_invoice")
        val signature = CryptoUtils.sign(data = hash, privateKey = Hex.decode(keyPair.privateKey))

        val result = CryptoUtils.verify(
            signature = signature,
            hash = hash,
            pubKey = Hex.decode(keyPair.pubKey),
        )

        assertTrue(result)
    }

    @Test
    fun verify_returnsFalse_whenSignatureCheckedAgainstDifferentPubKey() {
        val signer = CryptoUtils.generateHexEncodedKeypair()
        val impostor = CryptoUtils.generateHexEncodedKeypair()
        val hash = hashOf("pay_invoice")
        val signature = CryptoUtils.sign(data = hash, privateKey = Hex.decode(signer.privateKey))

        val result = CryptoUtils.verify(
            signature = signature,
            hash = hash,
            pubKey = Hex.decode(impostor.pubKey),
        )

        assertFalse(result)
    }

    @Test
    fun verify_returnsFalse_whenSignedContentWasTamperedWith() {
        val keyPair = CryptoUtils.generateHexEncodedKeypair()
        val signature = CryptoUtils.sign(
            data = hashOf("pay 1000 sats"),
            privateKey = Hex.decode(keyPair.privateKey),
        )

        val result = CryptoUtils.verify(
            signature = signature,
            hash = hashOf("pay 9999999 sats"),
            pubKey = Hex.decode(keyPair.pubKey),
        )

        assertFalse(result)
    }
}
