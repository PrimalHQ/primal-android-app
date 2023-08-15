package net.primal.android.user.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NostrWalletConnectParserTest {

    private val functioningNostrWalletConnectUrl = "nostr+walletconnect://69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9?relay=wss://relay.getalby.com/v1&secret=7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd&lud16=nikola@getalby.com"

    @Test
    fun `parseNWCUrl parses nostr wallet`() {
        val actual = functioningNostrWalletConnectUrl.parseNWCUrl()

        actual.pubkey shouldBe "69effe7b49a6dd5cf525bd0905917a5005ffe480b58eeb8e861418cf3ae760d9"
        actual.lud16 shouldBe "nikola@getalby.com"
        actual.relayUrl shouldBe "wss://relay.getalby.com/v1"
        actual.secret shouldBe "7c0dabd065b2de3299a0d0e1c26b8ac7047dae6b20aba3a62b23650eb601bbfd"
    }

    @Test
    fun `parseNWCUrl throws NWCParseException for invalid url`() {
        shouldThrow<NWCParseException> {
            "nostr+walletconnect://invalidNwcUrl".parseNWCUrl()
        }
    }
}
