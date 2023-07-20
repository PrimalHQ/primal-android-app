package net.primal.android.serialization

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import net.primal.android.nostr.model.primal.PrimalResourceVariant
import org.junit.Test

class RoomCustomTypeConvertersTest {

    private val listOfJsonArrayJsonString = """
            [
                ["q","df0f3ee95b7aa0141c87ac0b52d78d0e34deb1805ebd8797f5336ff36a0973d"],
                ["p","7161154c52254b979f0c35bca6357316ca4a41d32d9e6478b2fb43ec0910ad2"],
                ["t","bitcoin"],
                ["t","grownostr"]
            ]
        """.trimIndent()

    @Test
    fun `listOfJsonArrayToString returns correct json String`() {
        val expectedJsonString = this.listOfJsonArrayJsonString

        val inputJsonArray = NostrJson.decodeFromString<List<JsonArray>>(expectedJsonString)
        val actualJsonString = RoomCustomTypeConverters().listOfJsonArrayToString(inputJsonArray)

        actualJsonString.shouldNotBeNull()
        actualJsonString shouldEqualJson expectedJsonString
    }

    @Test
    fun `listOfJsonArrayToString supports null`() {
        val actual = RoomCustomTypeConverters().listOfJsonArrayToString(null)
        actual shouldBe null
    }

    @Test
    fun `stringToListOfJsonArray returns correct List`() {
        val jsonString = this.listOfJsonArrayJsonString
        val expected = NostrJson.decodeFromString<List<JsonArray>>(jsonString)
        val actual = RoomCustomTypeConverters().stringToListOfJsonArray(jsonString)
        actual shouldBe expected
    }

    @Test
    fun `invalid input to stringToListOfJsonArray returns null`() {
        val actual = RoomCustomTypeConverters().stringToListOfJsonArray("giberish")
        actual shouldBe null
    }

    @Test
    fun `stringToListOfJsonArray supports null`() {
        val actual = RoomCustomTypeConverters().stringToListOfJsonArray(null)
        actual shouldBe null
    }


    @Test
    fun `listOfStringsToJsonString returns correct String`() {
        val input = listOf("https://primal.net", "wss://relay.primal.net")
        val expected = NostrJson.encodeToString(input)
        val actual = RoomCustomTypeConverters().listOfStringsToJsonString(input)

        actual.shouldNotBeNull()
        actual shouldEqualJson expected
    }

    @Test
    fun `listOfStringsToJsonString supports null`() {
        val actual = RoomCustomTypeConverters().listOfStringsToJsonString(null)
        actual shouldBe null
    }

    @Test
    fun `jsonStringToListOfStrings returns correct List`() {
        val expected = listOf("https://primal.net", "wss://relay.primal.net")
        val jsonString = NostrJson.encodeToString(expected)
        val actual = RoomCustomTypeConverters().jsonStringToListOfStrings(jsonString)
        actual shouldBe expected
    }

    @Test
    fun `invalid input to jsonStringToListOfStrings returns null `() {
        val actual = RoomCustomTypeConverters().jsonStringToListOfStrings("giberish")
        actual shouldBe null
    }

    @Test
    fun `jsonStringToListOfStrings supports null`() {
        val actual = RoomCustomTypeConverters().jsonStringToListOfStrings(null)
        actual shouldBe null
    }


    @Test
    fun `listOfPrimalResourceVariantToString returns correct String`() {
        val input = listOf(
            PrimalResourceVariant(
                mimeType = "image/png",
                height = 100,
                width = 100,
                size = "L",
                mediaUrl = "https://image.png"
            )
        )
        val expected = NostrJson.encodeToString(input)

        val actual = RoomCustomTypeConverters().listOfPrimalResourceVariantToString(input)
        actual.shouldNotBeNull()
        actual shouldEqualJson expected
    }

    @Test
    fun `listOfPrimalResourceVariantToString supports null`() {
        val actual = RoomCustomTypeConverters().listOfPrimalResourceVariantToString(null)
        actual shouldBe null
    }

    @Test
    fun `stringToListOfPrimalResourceVariant returns correct List`() {
        val input = listOf(
            PrimalResourceVariant(
                mimeType = "image/png",
                height = 100,
                width = 100,
                size = "L",
                mediaUrl = "https://image.png"
            )
        )
        val jsonString = NostrJson.encodeToString(input)
        val actual = RoomCustomTypeConverters().stringToListOfPrimalResourceVariant(jsonString)
        actual shouldBe input
    }

    @Test
    fun `invalid input to stringToListOfPrimalResourceVariant returns null`() {
        val actual = RoomCustomTypeConverters().stringToListOfPrimalResourceVariant("giberish")
        actual shouldBe null
    }

    @Test
    fun `stringToListOfPrimalResourceVariant supports null`() {
        val actual = RoomCustomTypeConverters().stringToListOfPrimalResourceVariant(null)
        actual shouldBe null
    }

}
