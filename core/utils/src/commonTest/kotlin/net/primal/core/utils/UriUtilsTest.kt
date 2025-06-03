package net.primal.core.utils

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotEndWith
import kotlin.test.Test

class UriUtilsTest {

    @Test
    fun `detectUrls should trim closing parenthesis after TLD`() {
        val text = "Check this: primal.net)"
        text.detectUrls() shouldBe listOf("primal.net")
    }

    @Test
    fun `detectUrls should trim closing square bracket after TLD`() {
        val text = "Visit [primal.net]"
        text.detectUrls() shouldBe listOf("primal.net")
    }

    @Test
    fun `detectUrls should trim closing curly brace after TLD`() {
        val text = "Here's a link: {primal.net}"
        text.detectUrls() shouldBe listOf("primal.net")
    }

    @Test
    fun `detectUrls should trim closing quote after TLD`() {
        val text = "Try visiting \"primal.net\" for more info."
        text.detectUrls() shouldBe listOf("primal.net")
    }

    @Test
    fun `detectUrls should trim punctuation and bracket combo`() {
        val text = "Check out [primal.net)."
        text.detectUrls() shouldBe listOf("primal.net")
    }

    @Test
    fun `detectUrls should extract multiple urls inside parentheses and trim trailing parenthesis`() {
        val content = "This links should work (such as primal.net and example.com)."

        val urls = content.detectUrls()

        urls shouldBe listOf("primal.net", "example.com")
    }

    @Test
    fun `detectUrls should not match triple-dot false positives`() {
        "Medicare...he".detectUrls().shouldBeEmpty()
    }

    @Test
    fun `detectUrls should not match domains without a proper TLD`() {
        "justadomain".detectUrls().shouldBeEmpty()
        "example.".detectUrls().shouldBeEmpty()
    }

    @Test
    fun `detectUrls should not match single-letter or too-short TLD`() {
        "http://example.c".detectUrls().shouldBeEmpty()
    }

    @Test
    fun `detectUrls should not match adjacent-dot domains`() {
        "Visit a..b for info".detectUrls().shouldBeEmpty()
    }

    @Test
    fun `detectUrls should trim trailing dot after parenthesis-wrapped url`() {
        val content = "Check this: (blossom.primal.net)."
        val urls = content.detectUrls()
        urls shouldBe listOf("blossom.primal.net")
    }

    @Test
    fun `detectUrls should trim trailing comma after bracket-wrapped url`() {
        val content = "[www.example.org],"
        val urls = content.detectUrls()
        urls shouldBe listOf("www.example.org")
    }

    @Test
    fun `detectUrls should trim trailing exclamation and question marks`() {
        val content = "Check this out: example.com?!"
        val urls = content.detectUrls()
        urls shouldBe listOf("example.com")
    }

    @Test
    fun `detectUrls should recognize complex url`() {
        val content = """
            Some random links about bitcoin:
            https://media.infosec.exchange/infosec.exchange/media_attachments/files/114/426/571/043/336/971/original/e5c23bb1b071328e.jpg
        """.trimIndent()

        val expectedUrls = content.detectUrls()

        expectedUrls.shouldNotBeNull()
        expectedUrls shouldBe listOf(
            "https://media.infosec.exchange/infosec.exchange/media_attachments" +
                "/files/114/426/571/043/336/971/original/e5c23bb1b071328e.jpg",
        )
    }

    @Test
    fun `detectUrls should recognize https and http`() {
        val content = """
            Some random links about bitcoin:
            http://bitcoinops.org/en/newsletters/2023/06/07/
            http://www.bitcoinops.org/en/newsletters/2023/06/07/
            https://bitcoinops.org/en/newsletters/2023/06/07/
            https://www.bitcoinops.org/en/newsletters/2023/06/07/
        """.trimIndent()

        val expectedUrls = content.detectUrls()

        expectedUrls.shouldNotBeNull()
        expectedUrls.size shouldBeExactly 4
    }

    @Test
    fun `detectUrls should recognize urls with brackets`() {
        val content = """
            Some random links about bitcoin:
            https://en.m.wikipedia.org/wiki/Bit_(money)
        """.trimIndent()

        val expectedUrls = content.detectUrls()

        expectedUrls shouldBe listOf("https://en.m.wikipedia.org/wiki/Bit_(money)")
        expectedUrls.size shouldBeExactly 1
    }

    @Test
    fun `detectUrls should recognize various url formats`() {
        val content = """
        Some random links about bitcoin:
        https://en.m.wikipedia.org/wiki/Bit_(money)
        Check out primal.net for more info!
        Visit us at www.primal.net or at https://primal.net
        Here's a link to a secure site: https://www.example.com/path/to/resource
        And a simple link: example.com
        Don't forget the test with brackets: https://example.com/page?(query)=1&sort=desc
        """.trimIndent()

        val expectedUrls = content.detectUrls()

        expectedUrls shouldBe listOf(
            "https://en.m.wikipedia.org/wiki/Bit_(money)",
            "primal.net",
            "www.primal.net",
            "https://primal.net",
            "https://www.example.com/path/to/resource",
            "example.com",
            "https://example.com/page?(query)=1&sort=desc",
        )
        expectedUrls.size shouldBeExactly 7
    }

    @Test
    fun `detectUrls should recognize urls with port numbers`() {
        val content = """
        A link with a port number:
        https://www.example.com:443/resource
        """.trimIndent()

        val expectedUrls = content.detectUrls()

        expectedUrls.shouldNotBeNull()
        expectedUrls shouldContainExactly listOf(
            "https://www.example.com:443/resource",
        )
    }

    @Test
    fun `detectUrls should return empty for invalid urls`() {
        val content = """
        Some random links:
        thisisnotalink
        http://
        www.
        example@com
        """.trimIndent()

        val expectedUrls = content.detectUrls()

        expectedUrls.shouldNotBeNull()
        expectedUrls.size shouldBe 0
    }

    @Test
    fun `detectUrls should not return urls with brackets`() {
        val hugeContent = MARKDOWN_WITH_LINKS
        hugeContent.shouldNotBeNull()

        val urls = hugeContent.detectUrls()
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
