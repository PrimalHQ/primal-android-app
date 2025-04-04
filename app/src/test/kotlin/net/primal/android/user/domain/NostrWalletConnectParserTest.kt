package net.primal.android.user.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.acinq.secp256k1.Hex
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.primal.domain.nostr.cryptography.utils.CryptoUtils
import net.primal.domain.nostr.cryptography.utils.toHex
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NostrWalletConnectParserTest {

    private val expectedPubkey = "69e23cd92e34922a95a5a1eb6893e82cd3af9150ae2ab5e8c2cd882ccd701159"
    private val expectedSecret = "2a181df0a822100be8687de612d616a42febe8d99a2f99fb550f150dc364da6e"
    private val functioningNostrWalletConnectUrl = "nostr+walletconnect://$expectedPubkey" +
        "?relay=wss://relay.getalby.com/v1&secret=$expectedSecret&lud16=nikola@getalby.com"

    @Test
    fun parseNWCUrl_parsesNostrWallet() {
        val actual = functioningNostrWalletConnectUrl.parseNWCUrl()

        actual.pubkey shouldBe expectedPubkey
        actual.lightningAddress shouldBe "nikola@getalby.com"
        actual.relays shouldBe listOf("wss://relay.getalby.com/v1")
        actual.keypair.privateKey shouldBe expectedSecret
        actual.keypair.pubkey shouldBe CryptoUtils.publicKeyCreate(Hex.decode(expectedSecret)).toHex()
    }

    @Test
    fun parseNWCUrl_throwsNWCParseExceptionForInvalidUrl() {
        shouldThrow<NWCParseException> {
            "nostr+walletconnect://invalidNwcUrl".parseNWCUrl()
        }
    }

    @Test
    fun isNwcUrl_returnsFalseForInvalidUrl() {
        "nostr+walletconnect://invalidNwcUrl".isNwcUrl() shouldBe false
    }

    @Test
    fun isNwcUrl_returnsTrueForValidUrl() {
        functioningNostrWalletConnectUrl.isNwcUrl() shouldBe true
    }

    @Test(expected = NWCParseException::class)
    fun parseNWCUrl_throwsNWCParseException_ifRelayIsMissing() {
        "nostr+walletconnect://$expectedPubkey" +
            "?$expectedSecret&lud16=nikola@getalby.com"
                .parseNWCUrl()
    }

    @Test(expected = NWCParseException::class)
    fun parseNWCUrl_throwsNWCParseException_ifLud16IsMissing() {
        "nostr+walletconnect://$expectedPubkey" +
            "?relay=wss://relay.getalby.com/v1&secret="
                .parseNWCUrl()
    }

    @Test(expected = NWCParseException::class)
    fun parseNWCUrl_throwsNWCParseException_ifSecretIsMissing() {
        "nostr+walletconnect://$expectedPubkey" +
            "?relay=wss://relay.getalby.com/v1$expectedSecret&lud16=nikola@getalby.com"
                .parseNWCUrl()
    }

    @Test(expected = NWCParseException::class)
    fun parseNWCUrl_throwsNWCParseException_ifSecretIsInvalid() {
        "nostr+walletconnect://$expectedPubkey" +
            "?relay=wss://relay.getalby.com/v1$expectedSecret&lud16=nikola@getalby.com&secret=111"
                .parseNWCUrl()
    }
}
