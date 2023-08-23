package net.primal.android.core.utils

import io.kotest.matchers.shouldBe
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `isValidNostrKey return true for valid nsec`() {
        val nsec = "nsec1j4d3l08h0s5a9uhmjmc68ejxfgz06lpw9sfj7gwvlgkyt3q0j22se7aevw"
        nsec.isValidNostrPrivateKey() shouldBe true
    }

    @Test
    fun `isValidNostrKey return false for invalid nsec`() {
        val nsec = "nsec2j4d3l08h0s5a9uhmjmc68ejxfgz06lpw9sfj7gwvlgkyt3q0j22se7aevw"
        nsec.isValidNostrPrivateKey() shouldBe false
    }

    @Test
    fun `isValidNostrKey return false for empty value`() {
        val actual = "".isValidNostrPrivateKey()
        actual shouldBe false
    }

    @Test
    fun `isValidNostrKey return false for null value`() {
        val actual = null.isValidNostrPrivateKey()
        actual shouldBe false
    }

    @Test
    fun `isValidNostrKey returns true for valid 32 bytes hex value of nsec`() {
        val hex = "955b1fbcf77c29d2f2fb96f1a3e6464a04fd7c2e2c132f21ccfa2c45c40f9295"
        hex.isValidNostrPrivateKey() shouldBe true
    }

    @Test
    fun `isValidNostrKey returns false for less than 32 bytes hex value`() {
        val hex = "955b1fbcf77c29d2f2fb96f1a3e6464a04fd7c2e2c132f21ccfa2c45c40f92"
        hex.isValidNostrPrivateKey() shouldBe false
    }

    @Test
    fun `isValidNostrKey returns false for more than 32 bytes hex value`() {
        val hex = "955b1fbcf77c29d2f2fb96f1a3e6464a04fd7c2e2c132f21ccfa2c45c40f929599"
        hex.isValidNostrPrivateKey() shouldBe false
    }
}
