package net.primal.android.wallet

import io.kotest.matchers.shouldBe
import net.primal.android.wallet.api.decodeLNUrlOrNull
import net.primal.android.wallet.api.parseAsLNUrlOrNull
import org.junit.Test

class LightningExtTest {

    @Test
    fun `parseAsLNUrlOrNull returns null for invalid lud16 address`() {
        "invalid@@here".parseAsLNUrlOrNull() shouldBe null
    }

    @Test
    fun `parseAsLNUrlOrNull returns null for empty string`() {
        "".parseAsLNUrlOrNull() shouldBe null
    }

    @Test
    fun `parseAsLNUrlOrNull returns lnurl for lud16 address`() {
        "alex@primal.net".parseAsLNUrlOrNull() shouldBe "https://primal.net/.well-known/lnurlp/alex"
    }

    @Test
    fun `decodeLNUrl() successfully decodes lnurl`() {
        val actual = ("lnurl1dp68gurn8ghj7ampd3kx2ar0veekzar0wd5xjtnrdakj7" +
                "tnhv4kxctttdehhwm30d3h82unvwqhk6ctcv3jk6ctjvdhs85fgps").decodeLNUrlOrNull()
        actual shouldBe "https://walletofsatoshi.com/.well-known/lnurlp/maxdemarco"
    }

    @Test
    fun `decodeLNUrl() returns null for invalid lnurls`() {
        "blabla".decodeLNUrlOrNull() shouldBe null
    }

    @Test
    fun `decodeLNUrl() returns null for malformed lnurls`() {
        "lnurl1dp68gurn8ghj7ampd3xxxxx".decodeLNUrlOrNull() shouldBe null
    }

    @Test
    fun `decodeLNUrl() returns null for lud16`() {
        "alex@primal.net".decodeLNUrlOrNull() shouldBe null
    }
}
