package net.primal.android.nostr.ext

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import net.primal.android.crypto.bech32ToHex
import org.junit.Test

class NostrResourcesTest {

    @Test
    fun `nostrUriToPubkey returns correct pubkey hex value`() {
        val input = "nostr:npub1w0rthyjyp2f5gful0gm2500pwyxfrx93a85289xdz0sd6hyef33sh2cu4x"
        val expected = "73c6bb92440a9344279f7a36aa3de1710c9198b1e9e8a394cd13e0dd5c994c63"
        val actual = input.nostrUriToPubkey()
        actual shouldBe expected
    }

    @Test
    fun `nostrUriToPubkey returns null for invalid npub nostr uri`() {
        val actual = "invalidNostrUri".nostrUriToPubkey()
        actual.shouldBeNull()
    }

    @Test
    fun `nostrUriToNoteId returns correct pubkey hex value`() {
        val input = "nostr:note1zwwmjqpaymqhjlq0tjezcyhpcx0hyh4m3u5p54tm7mmfxaqg03pq3w3d6g"
        val expected = "139db9003d26c1797c0f5cb22c12e1c19f725ebb8f281a557bf6f69374087c42"
        val actual = input.nostrUriToNoteId()
        actual shouldBe expected
    }

    @Test
    fun `nostrUriToNoteId returns null for invalid npub nostr uri`() {
        val actual = "invalidNostrUri".nostrUriToNoteId()
        actual.shouldBeNull()
    }

    @Test
    fun `nostrUriToNoteIdAndRelay return correct noteId and relayUrl`() {
        val input = "nostr:nevent1qqs8t7ahfwqegkrxcexs5l4fdawx000jza3n5kz2wp6" +
                "ggulapl5maecpzfmhxue69uhk7enxvd5xz6tw9ec82cszyqsd9xqs66jlj2c" +
                "ytt0q96a6mjgrd46pe35xkqzpt3ptggm0ujkj7qcyqqqqgfcdkyzv8"
        val actual = input.nostrUriToNoteIdAndRelay()
        actual.first shouldBe "75fbb74b81945866c64d0a7ea96f5c67bdf217633a584a70748473fd0fe9bee7"
        actual.second shouldBe "wss://offchain.pub"
    }

    @Test
    fun `nostrUriToNoteIdAndRelay returns nulls for invalid input`() {
        val actual = "invalidNostrUri".nostrUriToNoteIdAndRelay()
        actual.shouldNotBeNull()
        actual.first.shouldBeNull()
        actual.second.shouldBeNull()
    }

    @Test
    fun `extractProfileId extracts hex profile id from npub1 uri`() {
        val npub = "npub1sg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63q0uf63m"
        val actual = "nostr:$npub".extractProfileId()
        actual.shouldNotBeNull()
        actual shouldBe npub.bech32ToHex()
    }

    @Test
    fun `extractNoteId extracts hex profile id from note1 uri`() {
        val note = "note1ksld0gmpnu6fpmnf0nrmm66fdx9jln22s96clup6xz7m232g27cs779y8e"
        val actual = "nostr:$note".extractNoteId()
        actual.shouldNotBeNull()
        actual shouldBe note.bech32ToHex()
    }
}
