package net.primal.android.user.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.acinq.secp256k1.Hex
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toHex
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NostrWalletConnectParserTest {

    private val expectedPubkey = "69e23cd92e34922a95a5a1eb6893e82cd3af9150ae2ab5e8c2cd882ccd701159"
    private val expectedSecret = "2a181df0a822100be8687de612d616a42febe8d99a2f99fb550f150dc364da6e"
    private val functioningNostrWalletConnectUrl = "nostr+walletconnect://$expectedPubkey" +
            "?relay=wss://relay.getalby.com/v1&secret=$expectedSecret&lud16=nikola@getalby.com"

    @Test
    fun `parseNWCUrl parses nostr wallet`() {
        val actual = functioningNostrWalletConnectUrl.parseNWCUrl()

        actual.pubkey shouldBe expectedPubkey
        actual.lightningAddress shouldBe "nikola@getalby.com"
        actual.relays shouldBe listOf("wss://relay.getalby.com/v1")
        actual.keypair.privateKey shouldBe expectedSecret
        actual.keypair.pubkey shouldBe CryptoUtils.publicKeyCreate(Hex.decode(expectedSecret)).toHex()
    }

    @Test
    fun `parseNWCUrl throws NWCParseException for invalid url`() {
        shouldThrow<NWCParseException> {
            "nostr+walletconnect://invalidNwcUrl".parseNWCUrl()
        }
    }
}
