package net.primal.data.remote.api.importing

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.serialization.json.Json

class PrimalImportApiTest {

    @Test
    fun `resolveImportEventsResponse reads imported and errors counts`() {
        val response = Json.parseToJsonElement(
            """[{"kind":10000127,"content":"{\"imported\":1,\"errors\":0}"}]""",
        )

        val result = response.resolveImportEventsResponse()

        result?.imported shouldBe 1
        result?.errors shouldBe 0
    }

    @Test
    fun `resolveImportEventsResponse returns null on error object`() {
        val response = Json.parseToJsonElement("""{"error":"error"}""")

        response.resolveImportEventsResponse() shouldBe null
    }

    @Test
    fun `resolveImportEventsResponse returns null when result event is missing`() {
        val response = Json.parseToJsonElement("""[{"kind":1,"content":"{}"}]""")

        response.resolveImportEventsResponse() shouldBe null
    }
}
