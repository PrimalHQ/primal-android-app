package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.Test

class TranslationSanitizerTest {

    private val npub = "npub1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq90f3g"

    @Test
    fun `mask and restore round-trips a nostr uri`() {
        val content = "Follow nostr:$npub for more updates about bitcoin today"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.maskedText shouldNotContain npub
        masked.maskedText shouldContain "__PROTECTED_0__"
        TranslationSanitizer.restoreProtectedTokens(masked.maskedText, masked.tokens) shouldBe content
    }

    @Test
    fun `mask protects bare npub and note references`() {
        val content = "Great post by $npub, read it here"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens shouldBe listOf(npub)
        TranslationSanitizer.restoreProtectedTokens(masked.maskedText, masked.tokens) shouldBe content
    }

    @Test
    fun `mask protects nostr reference placeholders`() {
        val content = "Quoting this here #[0] and also #[12] for context"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens shouldBe listOf("#[0]", "#[12]")
    }

    @Test
    fun `mask protects urls without swallowing a closing parenthesis`() {
        val content = "Check this link (https://example.com/some/path) it is great"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens shouldBe listOf("https://example.com/some/path")
        TranslationSanitizer.restoreProtectedTokens(masked.maskedText, masked.tokens) shouldBe content
    }

    @Test
    fun `mask protects lightning invoices`() {
        val invoice = "lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfpg9scrgwpugp34xsejvtdghgqn0h9ynqp4sp5kgerxe5c3"
        val content = "Pay this invoice $invoice please and thank you"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens shouldBe listOf(invoice)
    }

    @Test
    fun `mask protects hashtags and mentions as whole tokens`() {
        val content = "Hola @amigo esto es #Bitcoin para todos nosotros"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens shouldBe listOf("@amigo", "#Bitcoin")
    }

    @Test
    fun `restore tolerates provider-mangled placeholders`() {
        val tokens = listOf("https://example.com/x")
        TranslationSanitizer.restoreProtectedTokens("Mira __ PROTECTED 0 __ ahora", tokens) shouldBe
            "Mira https://example.com/x ahora"
        TranslationSanitizer.restoreProtectedTokens("Mira _PROTECTED_0_ ahora", tokens) shouldBe
            "Mira https://example.com/x ahora"
    }

    @Test
    fun `mask protects nsec secret keys`() {
        val nsec = "nsec1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq90f3g"
        val content = "Do not share this $nsec with anyone ever please"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens shouldBe listOf(nsec)
        TranslationSanitizer.restoreProtectedTokens(masked.maskedText, masked.tokens) shouldBe content
    }

    @Test
    fun `mask protects testnet and regtest invoices and email addresses`() {
        val content = "Pay lntb123fakeinvoice0 or lnbcrt99fakeinvoice1, or mail me at john.doe+notes@example-mail.com please"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens.size shouldBe 3
        TranslationSanitizer.restoreProtectedTokens(masked.maskedText, masked.tokens, masked.placeholderPrefix) shouldBe content
    }

    @Test
    fun `mask protects bare 64-char hex strings`() {
        val hex = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"
        val content = "This is the event id $hex for reference purposes"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens shouldBe listOf(hex)
        TranslationSanitizer.restoreProtectedTokens(masked.maskedText, masked.tokens) shouldBe content
    }

    @Test
    fun `mask salts placeholders when content already contains placeholder-like text`() {
        val content = "This note mentions __PROTECTED_0__ literally and links https://example.com/page for context"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        masked.tokens shouldBe listOf("https://example.com/page")
        val restored = TranslationSanitizer.restoreProtectedTokens(
            masked.maskedText,
            masked.tokens,
            masked.placeholderPrefix,
        )
        restored shouldBe content
    }

    @Test
    fun `salted placeholders restore when mangled without touching user text`() {
        val content = "This note mentions __PROTECTED_0__ literally and links https://example.com/page for context"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        val placeholder = "${masked.placeholderPrefix}0__"
        val mangled = masked.maskedText.replace(placeholder, "___PROTECTED 0__")
        TranslationSanitizer.restoreProtectedTokens(
            mangled,
            masked.tokens,
            masked.placeholderPrefix,
        ) shouldBe content
    }

    @Test
    fun `tokens dropped by the provider are appended instead of lost`() {
        val content = "Read https://example.com/a and https://example.com/b for the full story today"
        val masked = TranslationSanitizer.maskProtectedTokens(content)
        val restored = TranslationSanitizer.restoreProtectedTokens(
            "Read __PROTECTED_0__ for the full story today",
            masked.tokens,
            masked.placeholderPrefix,
        )
        restored shouldBe "Read https://example.com/a for the full story today https://example.com/b"
    }

    @Test
    fun `mangled placeholder shapes in user text are preserved`() {
        val single = "See _PROTECTED_0_ here and https://example.com/page for more context today"
        val maskedSingle = TranslationSanitizer.maskProtectedTokens(single)
        TranslationSanitizer.restoreProtectedTokens(
            maskedSingle.maskedText,
            maskedSingle.tokens,
            maskedSingle.placeholderPrefix,
        ) shouldBe single

        val triple = "See ___PROTECTED_0___ here and https://example.com/page for more context today"
        val maskedTriple = TranslationSanitizer.maskProtectedTokens(triple)
        TranslationSanitizer.restoreProtectedTokens(
            maskedTriple.maskedText,
            maskedTriple.tokens,
            maskedTriple.placeholderPrefix,
        ) shouldBe triple
    }

    @Test
    fun `shouldOfferTranslation rejects tiny and token-only notes`() {
        TranslationSanitizer.shouldOfferTranslation("GM") shouldBe false
        TranslationSanitizer.shouldOfferTranslation("https://example.com/only-a-link-here") shouldBe false
        TranslationSanitizer.shouldOfferTranslation("This is a perfectly normal English sentence.") shouldBe true
    }

    @Test
    fun `extractTranslatableProse drops protected tokens`() {
        val prose = TranslationSanitizer.extractTranslatableProse(
            "Hola amigos https://example.com esto es una prueba #Bitcoin",
        )
        prose shouldNotContain "example.com"
        prose shouldNotContain "#Bitcoin"
        prose shouldContain "Hola amigos"
    }
}
