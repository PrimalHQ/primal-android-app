package net.primal.domain.nostr.utils

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StringUtilsTest {

    private val defaultOwnerId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"

    @Test
    fun `authorNameUiFriendly returns displayName if it is not empty`() {
        val expected = "bob"
        val actual = authorNameUiFriendly(displayName = expected, name = null, pubkey = defaultOwnerId)
        actual shouldBe expected
    }

    @Test
    fun `authorNameUiFriendly returns name if displayName is empty`() {
        val expected = "Uncle Bob"
        val actual = authorNameUiFriendly(displayName = null, name = expected, pubkey = defaultOwnerId)
        actual shouldBe expected
    }

    @Test
    fun `authorNameUiFriendly returns ellipsized npub if displayName and name are empty`() {
        val actual = authorNameUiFriendly(displayName = null, name = null, pubkey = defaultOwnerId)
        actual shouldBe defaultOwnerId.asEllipsizedNpub()
    }

    @Test
    fun `usernameUiFriendly returns name if it is not empty`() {
        val expected = "bob"
        val actual = usernameUiFriendly(displayName = null, name = expected, pubkey = defaultOwnerId)
        actual shouldBe expected
    }

    @Test
    fun `usernameUiFriendly returns displayName if name is empty`() {
        val expected = "Uncle Bob"
        val actual = usernameUiFriendly(displayName = expected, name = null, pubkey = defaultOwnerId)
        actual shouldBe expected
    }

    @Test
    fun `usernameUiFriendly returns ellipsized npub if displayName and name are empty`() {
        val actual = usernameUiFriendly(displayName = null, name = null, pubkey = defaultOwnerId)
        actual shouldBe defaultOwnerId.asEllipsizedNpub()
    }
}
