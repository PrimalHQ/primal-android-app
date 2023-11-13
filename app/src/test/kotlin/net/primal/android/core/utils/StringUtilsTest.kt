package net.primal.android.core.utils

import io.kotest.matchers.shouldBe
import org.junit.Test

class StringUtilsTest {

    @Test
    fun `isPrimalIdentifier returns true for correct domain`() {
        "alex@primal.net".isPrimalIdentifier() shouldBe true
    }

    @Test
    fun `isPrimalIdentifier returns false if domain is incorrect`() {
        "alex@appollo41.com".isPrimalIdentifier() shouldBe false
    }

    @Test
    fun `asEllipsizedNpub returns ellipsized npub`() {
        val expected = "npub1ky9...4sw0alex"
        val hex = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"
        hex.asEllipsizedNpub() shouldBe expected
    }

    @Test
    fun `ellipsizeMiddle returns the original size is small`() {
        val expected = "thisIsRandomString"
        val actual = expected.ellipsizeMiddle(size = expected.length)
        actual shouldBe expected
    }

    @Test
    fun `ellipsizeMiddle returns ellipsized string in the middle`() {
        val original = "thisIsRandomString"
        val expected = "this...ring"
        val actual = original.ellipsizeMiddle(size = 4)
        actual shouldBe expected
    }

    @Test
    fun `formatNip05Identifier returns full id`() {
        "aleksandar@appoll41.com".formatNip05Identifier() shouldBe "aleksandar@appoll41.com"
    }

    @Test
    fun `formatNip05Identifier returns domain name for naked ids`() {
        "_@appollo41.com".formatNip05Identifier() shouldBe "appollo41.com"
    }

    @Test
    fun `formatNip05Identifier returns empty string if domain is missing in naked ids`() {
        "_@".formatNip05Identifier() shouldBe ""
    }
}
