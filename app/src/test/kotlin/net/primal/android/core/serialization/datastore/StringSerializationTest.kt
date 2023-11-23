package net.primal.android.core.serialization.datastore

import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StringSerializationTest {

    @Test
    fun `defaultValue returns empty string`() {
        val serializer = StringSerializer()
        val actual = serializer.defaultValue
        actual shouldBe ""
    }

    @Test
    fun `readFrom returns proper String`() = runTest {
        val serializer = StringSerializer()

        val expected = "Hello World!"
        val inputStream = ByteArrayInputStream(expected.toByteArray())

        val actual = serializer.readFrom(inputStream)
        actual shouldBe expected
    }

    @Test
    fun `writeTo writes input to given outputStream`() = runTest {
        val serializer = StringSerializer()

        val expected = "Hello Nostr!"
        val outputStream = ByteArrayOutputStream()
        serializer.writeTo(expected, outputStream)

        val actual = String(outputStream.toByteArray())
        actual shouldBe expected
    }

}
