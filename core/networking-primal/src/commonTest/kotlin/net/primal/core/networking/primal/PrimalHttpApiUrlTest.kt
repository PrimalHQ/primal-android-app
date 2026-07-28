package net.primal.core.networking.primal

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PrimalHttpApiUrlTest {

    @Test
    fun `asPrimalHttpApiUrl maps secure socket url to https api url`() {
        "wss://cache1.primal.net/v1".asPrimalHttpApiUrl() shouldBe "https://cache1.primal.net/api"
    }

    @Test
    fun `asPrimalHttpApiUrl maps plain socket url to http api url`() {
        "ws://localhost/v1".asPrimalHttpApiUrl() shouldBe "http://localhost/api"
    }

    @Test
    fun `asPrimalHttpApiUrl retains non default port`() {
        "ws://localhost:8080/v1".asPrimalHttpApiUrl() shouldBe "http://localhost:8080/api"
    }

    @Test
    fun `asPrimalHttpApiUrl handles url without path`() {
        "wss://cache2.primal.net".asPrimalHttpApiUrl() shouldBe "https://cache2.primal.net/api"
    }

    @Test
    fun `asPrimalHttpApiUrl handles wallet host`() {
        "wss://wallet.primal.net/v1".asPrimalHttpApiUrl() shouldBe "https://wallet.primal.net/api"
    }
}
