package net.primal.android.profile.db

import io.kotest.matchers.shouldBe
import net.primal.android.core.utils.asEllipsizedNpub
import org.junit.Test

class ProfileMetadataExtTests {

    private fun buildProfileMetadata(
        displayName: String? = null,
        name: String? = null,
        ownerId: String = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"
    ): ProfileMetadata = ProfileMetadata(
        createdAt = 0,
        eventId = "eventId",
        raw = "raw",
        ownerId = ownerId,
        displayName = displayName,
        handle = name,
    )

    @Test
    fun `authorNameUiFriendly returns first displayName if it is not empty`() {
        val expected = "bob"
        val profileMetadata = buildProfileMetadata(displayName = expected)
        val actual = profileMetadata.authorNameUiFriendly()
        actual shouldBe expected
    }

    @Test
    fun `authorNameUiFriendly returns name if displayName is empty`() {
        val expected = "Uncle Bob"
        val profileMetadata = buildProfileMetadata(name = expected)
        val actual = profileMetadata.authorNameUiFriendly()
        actual shouldBe expected
    }

    @Test
    fun `authorNameUiFriendly returns ellipsized npub if displayName an name are empty`() {
        val ownerId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"
        val profileMetadata = buildProfileMetadata(ownerId = ownerId)
        val actual = profileMetadata.authorNameUiFriendly()
        actual shouldBe ownerId.asEllipsizedNpub()
    }

    @Test
    fun `userNameUiFriendly returns first name if it is not empty`() {
        val expected = "bob"
        val profileMetadata = buildProfileMetadata(name = expected)
        val actual = profileMetadata.userNameUiFriendly()
        actual shouldBe expected
    }

    @Test
    fun `userNameUiFriendly returns displayName if name is empty`() {
        val expected = "Uncle Bob"
        val profileMetadata = buildProfileMetadata(displayName = expected)
        val actual = profileMetadata.userNameUiFriendly()
        actual shouldBe expected
    }

    @Test
    fun `userNameUiFriendly returns ellipsized npub if displayName an name are empty`() {
        val ownerId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b"
        val profileMetadata = buildProfileMetadata(ownerId = ownerId)
        val actual = profileMetadata.userNameUiFriendly()
        actual shouldBe ownerId.asEllipsizedNpub()
    }
}
