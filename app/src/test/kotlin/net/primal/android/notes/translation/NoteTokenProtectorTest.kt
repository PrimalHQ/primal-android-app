package net.primal.android.notes.translation

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.Test

class NoteTokenProtectorTest {

    @Test
    fun `round-trip restores urls nostr refs invoices and mentions`() {
        val original = """
            Hola @alice mira https://primal.net/e/note1abc
            nostr:npub1w0rthyjyp2f5gful0gm2500pwyxfrx93a85289xdz0sd6hyef33sh2cu4x
            npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m
            pay lnbc888550n1pnp6fz9pp5als09l5nfj9pkqk7mpj6cz6075nd4v95ljz0p65n8zkz03p75t3sdp
            lno1qsgq2q9z and lnurl1qxyzabc cashuAeyJhbGciOiJub25lIn0 #nostr :wave:
            bc1qw508d6qejxtdg4y5r3zarvary0c5xq0k8h8h and 1BoatSLRHtKNngkdXEeobR76b53LETtpyT
        """.trimIndent()

        val protected = NoteTokenProtector.protect(original)
        protected.tokens.isEmpty() shouldBe false
        protected.masked shouldNotContain "https://"
        protected.masked shouldNotContain "npub1"
        protected.masked shouldNotContain "lnbc"
        protected.restore(protected.masked) shouldBe original
    }

    @Test
    fun `restore recovers placeholders mangled into ascii brackets`() {
        val original = "Visit https://primal.net and npub1acdefghjklmnpqrstuv"
        val protected = NoteTokenProtector.protect(original)
        val mangled = protected.masked
            .replace("⟦", "[[")
            .replace("⟧", "]]")

        protected.restore(mangled) shouldBe original
    }

    @Test
    fun `restore recovers single-bracket and loose NT placeholders`() {
        val original = "Pay lightning:lnbc1abcxyz and see nrelay1qqqqqq"
        val protected = NoteTokenProtector.protect(original)
        val single = protected.masked.replace("⟦", "[").replace("⟧", "]")
        protected.restore(single) shouldBe original

        val loose = protected.tokens.indices.fold(protected.masked) { acc, index ->
            acc.replace(NoteTokenProtector.placeholder(index), "NT$index")
        }
        protected.restore(loose) shouldBe original
    }

    @Test
    fun `protects bolt12 lnurl cashu and nrelay`() {
        val original = "offer lno1qsgqzzzz invoice lni1abcd lnurl1save cashuBabc123 nrelay1qqqhello"
        val protected = NoteTokenProtector.protect(original)
        protected.masked shouldContain "⟦NT"
        protected.restore(protected.masked) shouldBe original
    }

    @Test
    fun `protects hex event ids and nsec without leaking them to the provider text`() {
        val hex = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"
        val original = "id $hex nsec1qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"
        val protected = NoteTokenProtector.protect(original)
        protected.masked shouldNotContain hex
        protected.masked.lowercase() shouldNotContain "nsec1"
        protected.restore(protected.masked) shouldBe original
    }

    @Test
    fun `strips trailing punctuation from urls`() {
        val original = "See https://primal.net."
        val protected = NoteTokenProtector.protect(original)
        protected.tokens.single() shouldBe "https://primal.net"
        protected.restore(protected.masked) shouldBe original
    }

    @Test
    fun `token-only notes are not offered for translation`() {
        NoteTokenProtector.shouldOfferTranslation("https://primal.net") shouldBe false
        NoteTokenProtector.shouldOfferTranslation("npub1acdefghjklmnpqrstuv") shouldBe false
        NoteTokenProtector.shouldOfferTranslation("lnbc1abc") shouldBe false
        NoteTokenProtector.shouldOfferTranslation("#nostr") shouldBe false
    }

    @Test
    fun `short or letter-poor notes are not offered`() {
        NoteTokenProtector.shouldOfferTranslation("ok") shouldBe false
        NoteTokenProtector.shouldOfferTranslation("12345") shouldBe false
        NoteTokenProtector.shouldOfferTranslation("") shouldBe false
    }

    @Test
    fun `readable sentences are offered`() {
        NoteTokenProtector.shouldOfferTranslation("Hola amigos, ¿cómo están hoy?") shouldBe true
        NoteTokenProtector.shouldOfferTranslation("Bonjour le monde nostral") shouldBe true
    }

    @Test
    fun `ordered tokens restore in the correct positions`() {
        val original = "A https://a.example B https://b.example C"
        val protected = NoteTokenProtector.protect(original)
        val translated = protected.masked.replace("A", "X").replace("B", "Y").replace("C", "Z")
        protected.restore(translated) shouldBe "X https://a.example Y https://b.example Z"
    }
}
