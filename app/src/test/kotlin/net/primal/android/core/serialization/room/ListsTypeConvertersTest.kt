package net.primal.android.core.serialization.room

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import net.primal.android.core.serialization.json.NostrJson
import org.junit.Test

class ListsTypeConvertersTest {

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
        val actualJsonString = ListsTypeConverters().listOfJsonArrayToString(inputJsonArray)

        actualJsonString.shouldNotBeNull()
        actualJsonString shouldEqualJson expectedJsonString
    }

    @Test
    fun `listOfJsonArrayToString supports null`() {
        val actual = ListsTypeConverters().listOfJsonArrayToString(null)
        actual shouldBe null
    }

    @Test
    fun `stringToListOfJsonArray returns correct List`() {
        val jsonString = this.listOfJsonArrayJsonString
        val expected = NostrJson.decodeFromString<List<JsonArray>>(jsonString)
        val actual = ListsTypeConverters().stringToListOfJsonArray(jsonString)
        actual shouldBe expected
    }

    @Test
    fun `invalid input to stringToListOfJsonArray returns null`() {
        val actual = ListsTypeConverters().stringToListOfJsonArray("giberish")
        actual shouldBe null
    }

    @Test
    fun `stringToListOfJsonArray supports null`() {
        val actual = ListsTypeConverters().stringToListOfJsonArray(null)
        actual shouldBe null
    }


    @Test
    fun `listOfStringsToJsonString returns correct String`() {
        val input = listOf("https://primal.net", "wss://relay.primal.net")
        val expected = NostrJson.encodeToString(input)
        val actual = ListsTypeConverters().listOfStringsToJsonString(input)

        actual.shouldNotBeNull()
        actual shouldEqualJson expected
    }

    @Test
    fun `listOfStringsToJsonString supports null`() {
        val actual = ListsTypeConverters().listOfStringsToJsonString(null)
        actual shouldBe null
    }

    @Test
    fun `jsonStringToListOfStrings returns correct List`() {
        val expected = listOf("https://primal.net", "wss://relay.primal.net")
        val jsonString = NostrJson.encodeToString(expected)
        val actual = ListsTypeConverters().jsonStringToListOfStrings(jsonString)
        actual shouldBe expected
    }

    @Test
    fun `invalid input to jsonStringToListOfStrings returns null `() {
        val actual = ListsTypeConverters().jsonStringToListOfStrings("giberish")
        actual shouldBe null
    }

    @Test
    fun `jsonStringToListOfStrings supports null`() {
        val actual = ListsTypeConverters().jsonStringToListOfStrings(null)
        actual shouldBe null
    }
}
