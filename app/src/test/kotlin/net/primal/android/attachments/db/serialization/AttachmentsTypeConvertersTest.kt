package net.primal.android.attachments.db.serialization

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.events.db.serialization.EventUriTypeConverters
import net.primal.android.events.domain.CdnResourceVariant
import org.junit.Test

class AttachmentsTypeConvertersTest {

    @Test
    fun `listOfCdnResourceVariantToString returns correct String`() {
        val input = listOf(
            CdnResourceVariant(
                height = 100,
                width = 100,
                mediaUrl = "https://image.png",
            ),
        )
        val expected = NostrJson.encodeToString(input)

        val actual = EventUriTypeConverters().listOfCdnResourceVariantToString(input)
        actual.shouldNotBeNull()
        actual shouldEqualJson expected
    }

    @Test
    fun `listOfCdnResourceVariantToString supports null`() {
        val actual = EventUriTypeConverters().listOfCdnResourceVariantToString(null)
        actual shouldBe null
    }

    @Test
    fun `stringToListOfCdnResourceVariant returns correct List`() {
        val input = listOf(
            CdnResourceVariant(
                height = 100,
                width = 100,
                mediaUrl = "https://image.png",
            ),
        )
        val jsonString = NostrJson.encodeToString(input)
        val actual = EventUriTypeConverters().stringToListOfCdnResourceVariant(jsonString)
        actual shouldBe input
    }

    @Test
    fun `invalid input to stringToListOfCdnResourceVariant returns null`() {
        val actual = EventUriTypeConverters().stringToListOfCdnResourceVariant("giberish")
        actual shouldBe null
    }

    @Test
    fun `stringToListOfCdnResourceVariant supports null`() {
        val actual = EventUriTypeConverters().stringToListOfCdnResourceVariant(null)
        actual shouldBe null
    }
}
