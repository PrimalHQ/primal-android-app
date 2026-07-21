package net.primal.android.notes.feed.note.translation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTextSanitizerTest {
    @Test
    fun protectAndRestore_keepsNostrUrlsInvoicesAndBc1() {
        val input =
            "hola nostr:npub1abcxyzdeadbeef https://example.com/x #nostr lnbc1testinvoice " +
                "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh :smile:"
        val protected = NoteTextSanitizer.protect(input)
        assertTrue(protected.text.contains("⟦T"))
        assertFalse(protected.text.contains("https://example.com"))
        assertFalse(protected.text.contains("bc1qxy"))
        assertFalse(protected.text.contains("lnbc1testinvoice"))
        val restored = NoteTextSanitizer.restore(protected.text, protected.tokens)
        assertEquals(input, restored)
    }

    @Test
    fun protect_extractsMultipleTokensInOrder() {
        val input = "see https://a.test and https://b.test"
        val protected = NoteTextSanitizer.protect(input)
        assertEquals(2, protected.tokens.size)
        assertEquals("https://a.test", protected.tokens[0])
        assertEquals("https://b.test", protected.tokens[1])
    }

    @Test
    fun restore_toleratesProvidersThatDropUnicodeBrackets() {
        val tokens = listOf("https://example.com", "npub1abcxyzdeadbeef")
        val mangled = "hello [T0] and [T1] end"
        val restored = NoteTextSanitizer.restore(mangled, tokens)
        assertEquals("hello https://example.com and npub1abcxyzdeadbeef end", restored)
    }

    @Test
    fun protect_coversNrelayBech32() {
        val input = "relay nrelay1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq here"
        val protected = NoteTextSanitizer.protect(input)
        assertFalse(protected.text.contains("nrelay1"))
        assertEquals(input, NoteTextSanitizer.restore(protected.text, protected.tokens))
    }

    @Test
    fun protect_coversMentionsAndHashtags() {
        val input = "hello @alice and #nostr world"
        val protected = NoteTextSanitizer.protect(input)
        assertFalse(protected.text.contains("@alice"))
        assertFalse(protected.text.contains("#nostr"))
        assertEquals(input, NoteTextSanitizer.restore(protected.text, protected.tokens))
    }

    @Test
    fun protect_coversBolt12OfferLnurlCashuAndLightningUri() {
        val input =
            "offer lno1offerxyz invoice lightning:lnbc1viauri lnurl1abc cashuAabc123 end"
        val protected = NoteTextSanitizer.protect(input)
        assertFalse(protected.text.contains("lno1offer"))
        assertFalse(protected.text.contains("lightning:"))
        assertFalse(protected.text.contains("lnurl1"))
        assertFalse(protected.text.contains("cashuA"))
        assertEquals(input, NoteTextSanitizer.restore(protected.text, protected.tokens))
    }

    @Test
    fun shouldOfferTranslation_requiresLengthAndLetters() {
        assertFalse(shouldOfferTranslation("hi"))
        assertFalse(shouldOfferTranslation("123456789012345"))
        assertTrue(shouldOfferTranslation("This note is long enough to translate."))
    }
}
