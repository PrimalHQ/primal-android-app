package net.primal.android.core.utils

import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotEndWith
import org.junit.Test

class UriUtilsTest {

    @Test
    fun `parseUrls should recognize https and http`() {
        val content = """
            Some random links about bitcoin:
            http://bitcoinops.org/en/newsletters/2023/06/07/
            http://www.bitcoinops.org/en/newsletters/2023/06/07/
            https://bitcoinops.org/en/newsletters/2023/06/07/
            https://www.bitcoinops.org/en/newsletters/2023/06/07/
        """.trimIndent()

        val expectedUrls = content.parseUris()

        expectedUrls.shouldNotBeNull()
        expectedUrls.size shouldBeExactly 4
    }

    @Test
    fun `parseUrls should recognize urls with brackets`() {
        val content = """
            Some random links about bitcoin:
            https://en.m.wikipedia.org/wiki/Bit_(money)
        """.trimIndent()

        val expectedUrls = content.parseUris()

        expectedUrls shouldBe listOf("https://en.m.wikipedia.org/wiki/Bit_(money)")
        expectedUrls.size shouldBeExactly 1
    }

    @Test
    fun `parseUrls should recognize various url formats`() {
        val content = """
        Some random links about bitcoin:
        https://en.m.wikipedia.org/wiki/Bit_(money)
        Check out primal.net for more info!
        Visit us at www.primal.net or at https://primal.net
        Here's a link to a secure site: https://www.example.com/path/to/resource
        And a simple link: example.com
        Don't forget the test with brackets: https://example.com/page?(query)=1&sort=desc
    """.trimIndent()

        val expectedUrls = content.parseUris()

        expectedUrls shouldBe listOf(
            "primal.net",
            "www.primal.net",
            "https://primal.net",
            "https://www.example.com/path/to/resource",
            "example.com",
            "https://en.m.wikipedia.org/wiki/Bit_(money)",
            "https://example.com/page?(query)=1&sort=desc"
        )
        expectedUrls.size shouldBeExactly 7
    }

    @Test
    fun `parseUrls should not return urls with brackets`() {
        val hugeContent = javaClass.getResource("/core/release_notes.txt")?.readText()
        hugeContent.shouldNotBeNull()

        val urls = hugeContent.parseUris()
        urls.shouldNotBeNull()
        urls.forEach {
            it.shouldNotEndWith(")")
            it.shouldNotEndWith("]")
        }
    }

    @Test
    fun `extractTLD returns proper top level domain for tld links without www`() {
        "primal.net".extractTLD() shouldBe "primal.net"
    }

    @Test
    fun `extractTLD returns proper top level domain for tld links with www`() {
        "www.primal.net".extractTLD() shouldBe "primal.net"
    }

    @Test
    fun `extractTLD returns proper top level domain without www and without protocol`() {
        "primal.net/p/profile".extractTLD() shouldBe "primal.net"
    }

    @Test
    fun `extractTLD returns proper top level domain with www and without protocol`() {
        "www.primal.net/p/profile".extractTLD() shouldBe "primal.net"
    }

    @Test
    fun `extractTLD returns proper top level domain for https link with www`() {
        "https://www.primal.net/p/profile".extractTLD() shouldBe "primal.net"
    }

    @Test
    fun `extractTLD returns proper top level domain for https link without www`() {
        "https://primal.net/p/profile".extractTLD() shouldBe "primal.net"
    }

    @Test
    fun `extractTLD returns proper top level domain for http link with www`() {
        "http://www.primal.net/p/profile".extractTLD() shouldBe "primal.net"
    }

    @Test
    fun `extractTLD returns proper top level domain for http link without www`() {
        "http://primal.net/p/profile".extractTLD() shouldBe "primal.net"
    }

    @Test
    fun `extractTLD returns proper top level domain for link with subdomain`() {
        "https://m.youtube.com/watch?v=SAN5CKbZnD0".extractTLD() shouldBe "youtube.com"
    }

    @Test
    fun detectMimeTypeDoesNotBreakWithQueryInUrl() {
        val urlLink = "https://image.nostr.build/23aa118ad8eee6152dc080ec9084d29fc437880b7591114a2c206665282189bf.jpg" +
            "#m=image%2Fjpeg&dim=720x1280"
        urlLink.detectMimeType() shouldBe "image/jpeg"
    }

    @Test
    fun detectMimeTypeGracefullyIgnoresTLDs() {
        val urlLink = "https://unleashed.chat"
        urlLink.detectMimeType() shouldBe null
    }

    @Test
    fun extractExtensionFromUrl_takesExtensionSuccessfully() {
        val urlLink = "https://image.nostr.build/23aa118ad8eeec206665282189bf.jpeg"
        urlLink.extractExtensionFromUrl() shouldBe "jpeg"
    }

    @Test
    fun extractExtensionFromUrl_takesExtensionSuccessfullyOnUrlsWithQueryParams() {
        val urlLink = "https://image.nostr.build/23aa118ad8eee6152dc080ec9084d29fc437880b7591114a2c206665282189bf.jpg" +
            "#m=image%2Fjpeg&dim=720x1280"
        urlLink.extractExtensionFromUrl() shouldBe "jpg"
    }
}
