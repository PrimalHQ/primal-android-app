package net.primal.android.user.credentials

import androidx.datastore.core.CorruptionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.test.runTest
import net.primal.android.security.Encryption
import org.junit.Test

class CredentialsSerializationTest {

    private fun encryptionMock(
        decryptResult: String = """[{"npub":"dummyNpub", "nsec":"dummyNsec"}]""",
    ) = mockk<Encryption>(relaxed = true) {
        every { decrypt(any()) } returns decryptResult
    }

    @Test
    fun `defaultValue returns empty list`() {
        val serializer = CredentialsSerialization(mockk(), mockk())
        val actual = serializer.defaultValue
        actual shouldBe emptyList()
    }

    @Test
    fun `readFrom calls decrypt with proper inputStream`() = runTest {
        val encryptionMock = encryptionMock()
        val serializer = CredentialsSerialization(encryption = encryptionMock)

        val inputStream = mockk<InputStream>()
        serializer.readFrom(inputStream)

        verify {
            encryptionMock.decrypt(
                withArg { it shouldBe inputStream }
            )
        }
    }

    @Test
    fun `readFrom throws CorruptionException for invalid data`() = runTest {
        val encryptionMock = encryptionMock(decryptResult = "giberish")
        val serializer = CredentialsSerialization(encryption = encryptionMock)

        shouldThrow<CorruptionException> {
            serializer.readFrom(mockk())
        }
    }

    @Test
    fun `writeTo calls encrypt with proper outputStream`() = runTest {
        val encryptionMock = encryptionMock()
        val serializer = CredentialsSerialization(encryption = encryptionMock)

        val outputStream = mockk<OutputStream>()
        serializer.writeTo(listOf(), outputStream)

        coVerify {
            encryptionMock.encrypt(
                any(),
                withArg { it shouldBe outputStream },
            )
        }
    }

}
