package net.primal.android.user.domain

import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NostrWalletConnectTest {
    // yes, it was invalidated immediately, no funny business allowed
    private val functioningNostrWalletConnectUrl = "nostr+walletconnect://69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9?relay=wss://relay.getalby.com/v1&secret=7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd&lud16=nikola@getalby.com"

    @Test
    fun `parse happy path works`() {
        val actual = functioningNostrWalletConnectUrl.toNostrWalletConnect()

        actual.pubkey shouldBe "69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9"
        actual.lud16 shouldBe "nikola@getalby.com"
        actual.relayUrl shouldBe "wss://relay.getalby.com/v1"
        actual.keypair.hexVariant.privkey shouldBe "7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd"
    }
}