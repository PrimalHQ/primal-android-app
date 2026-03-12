package net.primal.data.local.serialization

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.links.CdnResourceVariant

class CdnTypeConvertersTest {

    @Test
    fun `listOfCdnResourceVariantToString returns correct String`() {
        val input = listOf(
            CdnResourceVariant(
                height = 100,
                width = 100,
                mediaUrl = "https://image.png",
            ),
        )
        val expected = input.encodeToJsonString()

        val actual = CdnTypeConverters().listOfCdnResourceVariantToString(input)
        actual.shouldNotBeNull()
        actual shouldEqualJson expected
    }

    @Test
    fun `listOfCdnResourceVariantToString supports null`() {
        val actual = CdnTypeConverters().listOfCdnResourceVariantToString(null)
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
        val jsonString = input.encodeToJsonString()
        val actual = CdnTypeConverters().stringToListOfCdnResourceVariant(jsonString)
        actual shouldBe input
    }

    @Test
    fun `invalid input to stringToListOfCdnResourceVariant returns null`() {
        val actual = CdnTypeConverters().stringToListOfCdnResourceVariant("giberish")
        actual shouldBe null
    }

    @Test
    fun `stringToListOfCdnResourceVariant supports null`() {
        val actual = CdnTypeConverters().stringToListOfCdnResourceVariant(null)
        actual shouldBe null
    }
}
