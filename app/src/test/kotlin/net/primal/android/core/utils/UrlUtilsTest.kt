package net.primal.android.core.utils

import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.Test

class UrlUtilsTest {

    @Test
    fun testParseUrls() {
        val content = """
            Some random links about bitcoin:
            https://bitcoinops.org/en/newsletters/2023/06/07/
            https://www.bitcoinops.org/en/newsletters/2023/06/07/
        """.trimIndent()

        val expectedUrls = content.parseUrls()

        expectedUrls.shouldNotBeNull()
        expectedUrls.size shouldBeExactly 2
    }

}
