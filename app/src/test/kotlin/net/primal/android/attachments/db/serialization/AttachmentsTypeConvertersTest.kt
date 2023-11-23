package net.primal.android.attachments.db.serialization

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.core.serialization.json.NostrJson
import org.junit.Test

class AttachmentsTypeConvertersTest {

    @Test
    fun `listOfCdnResourceVariantToString returns correct String`() {
        val input = listOf(
            CdnResourceVariant(
                height = 100,
                width = 100,
                mediaUrl = "https://image.png"
            )
        )
        val expected = NostrJson.encodeToString(input)

        val actual = AttachmentTypeConverters().listOfCdnResourceVariantToString(input)
        actual.shouldNotBeNull()
        actual shouldEqualJson expected
    }

    @Test
    fun `listOfCdnResourceVariantToString supports null`() {
        val actual = AttachmentTypeConverters().listOfCdnResourceVariantToString(null)
        actual shouldBe null
    }

    @Test
    fun `stringToListOfCdnResourceVariant returns correct List`() {
        val input = listOf(
            CdnResourceVariant(
                height = 100,
                width = 100,
                mediaUrl = "https://image.png"
            )
        )
        val jsonString = NostrJson.encodeToString(input)
        val actual = AttachmentTypeConverters().stringToListOfCdnResourceVariant(jsonString)
        actual shouldBe input
    }

    @Test
    fun `invalid input to stringToListOfCdnResourceVariant returns null`() {
        val actual = AttachmentTypeConverters().stringToListOfCdnResourceVariant("giberish")
        actual shouldBe null
    }

    @Test
    fun `stringToListOfCdnResourceVariant supports null`() {
        val actual = AttachmentTypeConverters().stringToListOfCdnResourceVariant(null)
        actual shouldBe null
    }

}
