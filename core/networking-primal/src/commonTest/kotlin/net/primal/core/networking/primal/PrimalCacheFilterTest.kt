package net.primal.core.networking.primal

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PrimalCacheFilterTest {

    @Test
    fun `toPrimalJsonArray builds positional verb and options request`() {
        val filter = PrimalCacheFilter(primalVerb = "import_events", optionsJson = """{"events":[]}""")

        filter.toPrimalJsonArray().toString() shouldBe """["import_events",{"events":[]}]"""
    }

    @Test
    fun `toPrimalJsonArray omits options when not provided`() {
        val filter = PrimalCacheFilter(primalVerb = "import_events")

        filter.toPrimalJsonArray().toString() shouldBe """["import_events"]"""
    }

    @Test
    fun `toPrimalJsonObject wraps the same request in a cache envelope`() {
        val filter = PrimalCacheFilter(primalVerb = "import_events", optionsJson = """{"events":[]}""")

        filter.toPrimalJsonObject().toString() shouldBe """{"cache":["import_events",{"events":[]}]}"""
    }
}
