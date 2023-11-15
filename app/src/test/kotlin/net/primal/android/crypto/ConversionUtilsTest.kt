package net.primal.android.crypto

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.spongycastle.util.encoders.DecoderException

class ConversionUtilsTest {

    @Test
    fun `hexToNoteHrp converts successfully`() {
        val expected = "note169r8yupjxwtpaxmhytetcyunk93mxh39ajpnyuuy7u4rxzsycvcsyddl07"
        val eventId = "d14672703233961e9b7722f2bc1393b163b35e25ec83327384f72a330a04c331"
        val actual = eventId.hexToNoteHrp()
        actual shouldBe expected
    }

    @Test
    fun `hexToNoteHrp throws DecoderException`() {
        shouldThrow<DecoderException> {
            "invalid".hexToNoteHrp()
        }
    }

    @Test
    fun `hexToNpubHrp converts successfully`() {
        val expected = "npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"
        val pubkeyHex = "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"
        val actual = pubkeyHex.hexToNpubHrp()
        actual shouldBe expected
    }

    @Test
    fun `hexToNpubHrp throws DecoderException`() {
        shouldThrow<DecoderException> {
            "invalid".hexToNpubHrp()
        }
    }

    @Test
    fun `hexToNsecHrp converts successfully`() {
        val expected = "nsec1vl029mgpspedva04g90vltkh6fvh240zqtv9k0t9af8935ke9laqsnlfe5"
        val privateKeyHex = "67dea2ed018072d675f5415ecfaed7d2597555e202d85b3d65ea4e58d2d92ffa"
        val actual = privateKeyHex.hexToNsecHrp()
        actual shouldBe expected
    }

    @Test
    fun `hexToNsecHrp throws DecoderException`() {
        shouldThrow<DecoderException> {
            "invalid".hexToNsecHrp()
        }
    }

    @Test
    fun `bech32ToHex converts nsec successfully`() {
        val expected = "67dea2ed018072d675f5415ecfaed7d2597555e202d85b3d65ea4e58d2d92ffa"
        val nsec = "nsec1vl029mgpspedva04g90vltkh6fvh240zqtv9k0t9af8935ke9laqsnlfe5"
        val actual = nsec.bech32ToHex()
        actual shouldBe expected
    }

    @Test
    fun `bech32ToHex converts npub successfully`() {
        val expected = "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"
        val npub = "npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"
        val actual = npub.bech32ToHex()
        actual shouldBe expected
    }

    @Test
    fun `bech32ToHex converts note successfully`() {
        val expected = "d14672703233961e9b7722f2bc1393b163b35e25ec83327384f72a330a04c331"
        val note = "note169r8yupjxwtpaxmhytetcyunk93mxh39ajpnyuuy7u4rxzsycvcsyddl07"
        val actual = note.bech32ToHex()
        actual shouldBe expected
    }

    @Test
    fun `bech32ToHex throws IllegalArgumentException`() {
        shouldThrow<IllegalArgumentException> {
            "invalid".bech32ToHex()
        }
    }
}
