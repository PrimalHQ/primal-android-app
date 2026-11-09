package net.primal.android.translation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.Test

class TranslationSanitizerTest {
    @Test
    fun `sanitizeAndRestore preserves nostr urls hashtags and mentions`() {
        val raw = "hola https://primal.net note1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq #tag @alice"
        val sanitized = TranslationSanitizer.sanitizeForTranslation(raw)
        sanitized.content shouldContain "__PRIMAL_TX_0__"
        sanitized.content shouldNotContain "https://primal.net"
        val restored = TranslationSanitizer.restoreTranslationContent(
            "hello __PRIMAL_TX_0__ __PRIMAL_TX_1__ __PRIMAL_TX_2__ __PRIMAL_TX_3__",
            sanitized.placeholders,
        )
        restored shouldContain "https://primal.net"
        restored shouldContain "#tag"
        restored shouldContain "@alice"
    }

    @Test
    fun `restore handles mangled placeholders`() {
        val placeholders = listOf("npub1abc")
        val restored = TranslationSanitizer.restoreTranslationContent("see PRIMAL-TX-0 please", placeholders)
        restored shouldBe "see npub1abc please"
    }

    @Test
    fun `shouldOfferTranslation requires substance`() {
        TranslationSanitizer.shouldOfferTranslation("hi") shouldBe false
        TranslationSanitizer.shouldOfferTranslation("This is a longer note about sports.") shouldBe true
    }
}
