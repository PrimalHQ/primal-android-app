package net.primal.android.wallet.utils

import io.kotest.matchers.shouldBe
import net.primal.android.wallet.utils.ZapConversionUtils.formatAsString
import net.primal.android.wallet.utils.ZapConversionUtils.toBtc
import net.primal.android.wallet.utils.ZapConversionUtils.toSats
import org.junit.Test

class ZapConversionUtilsTest {

    @Test
    fun satsToBtc_1_sat() {
        1.toBtc() shouldBe 0.00_000_001
    }

    @Test
    fun satsToBtc_ten_sats() {
        10.toBtc() shouldBe 0.00_000_010
    }

    @Test
    fun satsToBtc_hundred_sats() {
        100.toBtc() shouldBe 0.00_000_1
    }

    @Test
    fun satsToBtc_1K_sats() {
        1_000.toBtc() shouldBe 0.00_001
    }

    @Test
    fun satsToBtc_10K_sats() {
        10_000L.toBtc() shouldBe 0.00_01
    }

    @Test
    fun satsToBtc_100K_sats() {
        100_000L.toBtc() shouldBe 0.00_1
    }

    @Test
    fun satsToBtc_1M_sats() {
        1_000_000L.toBtc() shouldBe 0.01
    }

    @Test
    fun satsToBtc_100M_sats() {
        100_000_000L.toULong().toBtc() shouldBe 1.00
    }

    @Test
    fun satsToBtc_1024M_sats() {
        1_024_000_000L.toULong().toBtc() shouldBe 10.24
    }

    @Test
    fun btcToSats_ten_sats() {
        0.00_000_010.toSats() shouldBe 10L.toULong()
    }

    @Test
    fun btcToSats_hundred_sats() {
        0.00_000_100.toSats() shouldBe 100L.toULong()
    }

    @Test
    fun btcToSats_1K_sats() {
        0.00_001_000.toSats() shouldBe 1_000L.toULong()
    }

    @Test
    fun btcToSats_100K_sats() {
        0.00_100_000.toSats() shouldBe 100_000L.toULong()
    }

    @Test
    fun btcToSats_100M_sats() {
        1.00_000_000.toSats() shouldBe 100_000_000L.toULong()
    }

    @Test
    fun satsToBtc_1K_toStringSats() {
        1_000.toBtc().formatAsString() shouldBe "0.00001"
    }

    @Test
    fun satsToBtc_10K_toStringSats() {
        10_000.toBtc().formatAsString() shouldBe "0.0001"
    }

    @Test
    fun satsToBtc_100K_toStringSats() {
        100_000.toBtc().formatAsString() shouldBe "0.001"
    }

    @Test
    fun satsToBtc_1M_toStringSats() {
        1_000_000.toBtc().formatAsString() shouldBe "0.01"
    }

    @Test
    fun satsToBtc_10M_toStringSats() {
        10_000_000.toBtc().formatAsString() shouldBe "0.1"
    }

    @Test
    fun satsToBtc_100M_toStringSats() {
        100_000_000.toBtc().formatAsString() shouldBe "1"
    }

    @Test
    fun satsToBtc_128M_toStringSats() {
        128_000_000.toBtc().formatAsString() shouldBe "1.28"
    }

    @Test
    fun satsToBtc_1B_toStringSats() {
        1_000_000_000.toBtc().formatAsString() shouldBe "10"
    }
}
