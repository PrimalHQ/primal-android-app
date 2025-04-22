package net.primal.domain.nostr.utils

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import net.primal.domain.nostr.cryptography.utils.bech32ToHexOrThrow

class NostrUriUtilsTest {

    @Test
    fun nostrUriToPubkey_returnsCorrectPubkeyHexValue() {
        val input = "nostr:npub1w0rthyjyp2f5gful0gm2500pwyxfrx93a85289xdz0sd6hyef33sh2cu4x"
        val expected = "73c6bb92440a9344279f7a36aa3de1710c9198b1e9e8a394cd13e0dd5c994c63"
        val actual = input.nostrUriToPubkey()
        actual shouldBe expected
    }

    @Test
    fun nostrUriToPubkey_returnsNullForInvalidNpubNostrUri() {
        val actual = "invalidNostrUri".nostrUriToPubkey()
        actual.shouldBeNull()
    }

    @Test
    fun nostrUriToNoteId_returnsNullForIncompleteNpubNostrUri() {
        val actual = "nostr:npub121212".nostrUriToNoteId()
        actual.shouldBeNull()
    }

    @Test
    fun nostrUriToNoteId_returnsCorrectPubkeyHexValue() {
        val input = "nostr:note1zwwmjqpaymqhjlq0tjezcyhpcx0hyh4m3u5p54tm7mmfxaqg03pq3w3d6g"
        val expected = "139db9003d26c1797c0f5cb22c12e1c19f725ebb8f281a557bf6f69374087c42"
        val actual = input.nostrUriToNoteId()
        actual shouldBe expected
    }

    @Test
    fun nostrUriToNoteId_returnsNullForInvalidNoteNostrUri() {
        val actual = "invalidNostrUri".nostrUriToNoteId()
        actual.shouldBeNull()
    }

    @Test
    fun nostrUriToNoteId_returnsNullForIncompleteNostrUri() {
        val actual = "nostr:note12fjfj".nostrUriToNoteId()
        actual.shouldBeNull()
    }

    @Test
    fun nostrUriToNoteIdAndRelay_returnCorrectNoteIdAndRelayUrl() {
        val input = "nostr:nevent1qqs8t7ahfwqegkrxcexs5l4fdawx000jza3n5kz2wp6" +
            "ggulapl5maecpzfmhxue69uhk7enxvd5xz6tw9ec82cszyqsd9xqs66jlj2c" +
            "ytt0q96a6mjgrd46pe35xkqzpt3ptggm0ujkj7qcyqqqqgfcdkyzv8"
        val actual = input.nostrUriToNoteIdAndRelay()
        actual.first shouldBe "75fbb74b81945866c64d0a7ea96f5c67bdf217633a584a70748473fd0fe9bee7"
        actual.second shouldBe "wss://offchain.pub"
    }

    @Test
    fun nostrUriToNoteIdAndRelay_returnsNullsForInvalidInput() {
        val actual = "invalidNostrUri".nostrUriToNoteIdAndRelay()
        actual.shouldNotBeNull()
        actual.first.shouldBeNull()
        actual.second.shouldBeNull()
    }

    @Test
    fun extractProfileId_extractsHexProfileIdFromNpub1Uri() {
        val npub = "npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m"
        val actual = "nostr:$npub".extractProfileId()
        actual.shouldNotBeNull()
        actual shouldBe npub.bech32ToHexOrThrow()
    }

    @Test
    fun extractNoteId_extractsHexProfileIdFromNote1Uri() {
        val note = "note1ksld0gmpnu6fpmnf0nrmm66fdx9jln22s96clup6xz7m232g27cs779y8e"
        val actual = "nostr:$note".extractNoteId()
        actual.shouldNotBeNull()
        actual shouldBe note.bech32ToHexOrThrow()
    }

    @Test
    fun parseNostrUris_returnsListWithSingleValidNoteUri() {
        val parsed = "note122p22tp49v3u2q0v0y0qk0cv07xxl8krs6z3y9x0ergx57ejl77qgray5l".parseNostrUris()
        parsed shouldContain "note122p22tp49v3u2q0v0y0qk0cv07xxl8krs6z3y9x0ergx57ejl77qgray5l"
    }

    @Test
    fun parseNostrUris_returnsListWithMultipleValidNoteUris() {
        val parsed = (
            "Some notes: " +
                "note122p22tp49v3u2q0v0y0qk0cv07xxl8krs6z3y9x0ergx57ejl77qgray5l " +
                "note17lf0wvdd4v2wvckrx550waytxryeq5k9cr9q8swnf43wrjhhhk0sh82aez " +
                "some footer text."
            ).parseNostrUris()
        parsed shouldContain "note122p22tp49v3u2q0v0y0qk0cv07xxl8krs6z3y9x0ergx57ejl77qgray5l"
        parsed shouldContain "note17lf0wvdd4v2wvckrx550waytxryeq5k9cr9q8swnf43wrjhhhk0sh82aez"
    }

    @Test
    fun parseNostrUris_returnsEmptyListIfNoNoteUris() {
        val parsed = "There are no notes here.".parseNostrUris()
        parsed.shouldBeEmpty()
    }

    @Test
    fun parseNostrUris_skipsInvalidNoteUris() {
        val parsed = (
            "There is just one note here: " +
                "note12fjf " +
                "note17lf0wvdd4v2wvckrx550waytxryeq5k9cr9q8swnf43wrjhhhk0sh82aez"
            ).parseNostrUris()
        parsed shouldContain "note17lf0wvdd4v2wvckrx550waytxryeq5k9cr9q8swnf43wrjhhhk0sh82aez"
        parsed.size shouldBe 1
    }

    @Test
    fun parseNostrUris_skipsInvalidNpubAndNoteUris() {
        val parsed = (
            "There is just one note and npub here: " +
                "note12fjf " +
                "note17lf0wvdd4v2wvckrx550waytxryeq5k9cr9q8swnf43wrjhhhk0sh82aez " +
                "npub121212 " +
                "npub17nd4yu9anyd3004pumgrtazaacujjxwzj36thtqsxskjy0r5urgqf6950x " +
                "and some footer text."
            ).parseNostrUris()
        println(parsed)
        parsed shouldContain "note17lf0wvdd4v2wvckrx550waytxryeq5k9cr9q8swnf43wrjhhhk0sh82aez"
        parsed shouldContain "npub17nd4yu9anyd3004pumgrtazaacujjxwzj36thtqsxskjy0r5urgqf6950x"
        parsed.size shouldBe 2
    }
}
