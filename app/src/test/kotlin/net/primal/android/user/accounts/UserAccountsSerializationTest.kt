package net.primal.android.user.accounts

import androidx.datastore.core.CorruptionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import net.primal.android.security.Encryption
import net.primal.android.user.domain.UserAccount
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream

class UserAccountsSerializationTest {

    private fun encryptionMock(
        decryptResult: String = """
            [{   
                "pubkey":"b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                "authorDisplayName":"Alex",
                "userDisplayName":"alex"
            }]
        """.trimIndent(),
    ) = mockk<Encryption>(relaxed = true) {
        every { decrypt(any()) } returns decryptResult
    }

    @Test
    fun `defaultValue returns empty list`() {
        val serializer = UserAccountsSerialization(mockk(), mockk())
        val actual = serializer.defaultValue
        actual shouldBe emptyList()
    }

    @Test
    fun `readFrom calls decrypt with proper inputStream`() = runTest {
        val encryptionMock = encryptionMock()
        val serializer = UserAccountsSerialization(encryption = encryptionMock)

        val inputStream = mockk<InputStream>()
        serializer.readFrom(inputStream)

        verify {
            encryptionMock.decrypt(
                withArg { it shouldBe inputStream },
            )
        }
    }

    @Test
    fun `readFrom throws CorruptionException for invalid data`() = runTest {
        val encryptionMock = encryptionMock(decryptResult = "giberish")
        val serializer = UserAccountsSerialization(encryption = encryptionMock)

        shouldThrow<CorruptionException> {
            serializer.readFrom(mockk())
        }
    }

    @Test
    fun `writeTo calls encrypt with proper outputStream`() = runTest {
        val encryptionMock = encryptionMock()
        val serializer = UserAccountsSerialization(encryption = encryptionMock)

        val outputStream = mockk<OutputStream>()
        serializer.writeTo(listOf(UserAccount.EMPTY), outputStream)

        coVerify {
            encryptionMock.encrypt(
                any(),
                withArg { it shouldBe outputStream },
            )
        }
    }
}
