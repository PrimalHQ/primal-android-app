package net.primal.data.repository.mappers.remote

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.domain.links.CdnResource
import net.primal.domain.links.CdnResourceVariant

class ResolveDurationInSecondsTest {

    private fun cdn(vararg durations: Double?): CdnResource =
        CdnResource(
            url = "u",
            variants = durations.map { d ->
                CdnResourceVariant(width = 10, height = 10, mediaUrl = "u", durationInSeconds = d)
            },
        )

    private fun imeta(duration: String?): JsonArray =
        buildJsonArray {
            add("imeta")
            add("url https://example.com/video.mp4")
            if (duration != null) add("duration $duration")
        }

    @Test
    fun `returns CDN duration when positive`() {
        resolveDurationInSeconds(cdn(25.5), imeta("99.0")) shouldBe 25.5
    }

    @Test
    fun `returns first positive variant duration`() {
        resolveDurationInSeconds(cdn(null, 42.0), null) shouldBe 42.0
    }

    @Test
    fun `falls back to IMeta when CDN missing`() {
        resolveDurationInSeconds(cdnResource = null, imetaTag = imeta("17.5")) shouldBe 17.5
    }

    @Test
    fun `falls back to IMeta when CDN has no positive variant`() {
        resolveDurationInSeconds(cdn(null, null), imeta("17.5")) shouldBe 17.5
    }

    @Test
    fun `returns null when both sources missing`() {
        resolveDurationInSeconds(cdnResource = null, imetaTag = null) shouldBe null
    }

    @Test
    fun `returns null when IMeta has zero or negative`() {
        resolveDurationInSeconds(cdnResource = null, imetaTag = imeta("0")) shouldBe null
        resolveDurationInSeconds(cdnResource = null, imetaTag = imeta("-5")) shouldBe null
    }

    @Test
    fun `returns null when CDN variants empty and IMeta missing`() {
        resolveDurationInSeconds(cdn(), null) shouldBe null
    }
}
