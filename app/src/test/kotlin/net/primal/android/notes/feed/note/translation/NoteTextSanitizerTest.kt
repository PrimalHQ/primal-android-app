package net.primal.android.notes.feed.note.translation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTextSanitizerTest {
    @Test
    fun protectAndRestore_keepsNostrAndUrls() {
        val input =
            "hola nostr:npub1abcxyzdeadbeef https://example.com/x #nostr lnbc1testinvoice :smile:"
        val protected = NoteTextSanitizer.protect(input)
        assertTrue(protected.text.contains("⟦T"))
        assertFalse(protected.text.contains("https://example.com"))
        // Simulate a translator that leaves placeholders intact
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
}
