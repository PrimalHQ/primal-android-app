package net.primal.android.wallet.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalletStringUtilsTest {

    private val validLnInvoice = "lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqy" +
        "pqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafn" +
        "h3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w"

    @Test
    fun isLnInvoice_returnsTrueForCorrectLightningInvoice() {
        validLnInvoice.isLnInvoice() shouldBe true
    }

    @Test
    fun isLnInvoice_returnsFalseForWrongPrefix() {
        "bitcoin:something".isLnInvoice() shouldBe false
    }

    @Test
    fun isLnInvoice_returnsTrueForPartialLnbcString() {
        "lnbcblablabla".isLnInvoice() shouldBe true
    }

    @Test
    fun isLnUrl_returnsTrueForCorrectLnUrl() {
        "lnurl1dp68gurn8ghj7urjd9kkzmpwdejhgtewwajkcmpdddhx7amw9akxuatjd3cz7ctvv4uqjeypkv".isLnUrl() shouldBe true
    }

    @Test
    fun isLnUrl_returnsFalseForInvalidLnUrl() {
        "somethingRandom".isLnUrl() shouldBe false
    }

    @Test
    fun isLnUrl_returnsTrueForPartialLnUrlString() {
        "lnurl1abcdefgh".isLnUrl() shouldBe true
    }

    @Test
    fun isLightningAddressUri_returnTrueForCorrectLightningLud16Uri() {
        "lightning:alex@primal.net".isLightningAddressUri() shouldBe true
    }

    @Test
    fun isLightningAddressUri_returnTrueForCorrectLightningLnUrlUri() {
        "lightning:lnurl1dp68gurn8ghj7urjd9kkzmpwdejhgtewwajkcmpdddhx7amw9akxuatjd3cz7ctvv4uqjeypkv"
            .isLightningAddressUri() shouldBe true
    }

    @Test
    fun isLightningAddressUri_returnFalseForMissingProtocol() {
        "alex@primal.net".isLightningAddressUri() shouldBe false
    }

    @Test
    fun isLightningAddressUri_returnFalseForMissingLud16OrLnUrl() {
        "lightning:somethingInvalid".isLightningAddressUri() shouldBe false
    }

    @Test
    fun isLightningAddressUri_returnTrueForCorrectLightningInvoice() {
        "lightning:$validLnInvoice".isLightningAddressUri() shouldBe true
    }

    @Test
    fun isBitcoinAddressUri_returnsTrueForCorrectBitcoinUri() {
        "bitcoin:bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw".isBitcoinAddressUri() shouldBe true
    }

    @Test
    fun isBitcoinAddressUri_returnsFalseForMissingProtocol() {
        "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw".isBitcoinAddressUri() shouldBe false
    }

    @Test
    fun isBitcoinAddressUri_returnsFalseForInvalidBtcAddress() {
        "bitcoin:butInvalidBtcAddress".isBitcoinAddressUri() shouldBe false
    }

    @Test
    fun isBitcoinAddress_returnsTrueForCorrectBtcAddress() {
        "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw".isBitcoinAddress() shouldBe true
    }

    @Test
    fun isBitcoinAddress_returnsFalseForInvalidBtcAddress() {
        "bc1invalidbitcoinaddress".isBitcoinAddress() shouldBe false
    }

    @Test
    fun isBitcoinAddress_returnsFalseForNullString() {
        null.isBitcoinAddress() shouldBe false
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnNullForInvalidString() {
        "bc1balblablablabl".parseBitcoinPaymentInstructions() shouldBe null
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnBtcAddressIfInputIsBtcAddress() {
        "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw".parseBitcoinPaymentInstructions() shouldBe
            BitcoinPaymentInstruction(address = "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw")
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnBtcAddressIfInputIsBtcAddressUri() {
        "bitcoin:bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw".parseBitcoinPaymentInstructions() shouldBe
            BitcoinPaymentInstruction(address = "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw")
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnNullForInvalidBtcAddressIsInBitcoinUri() {
        "bitcoin:bc1q99ygpnytwmss4am6ytessw&".parseBitcoinPaymentInstructions() shouldBe null
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnBtcAddressAndAmountIfInputIsHasValidAddressAndAmount() {
        "bitcoin:bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw?amount=1.0".parseBitcoinPaymentInstructions() shouldBe
            BitcoinPaymentInstruction(
                address = "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw",
                amount = "1.0",
            )
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnBtcAddressAndLabelIfInputIsHasValidAddressAndLabel() {
        "bitcoin:bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw?label=MyComment".parseBitcoinPaymentInstructions() shouldBe
            BitcoinPaymentInstruction(
                address = "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw",
                label = "MyComment",
            )
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnBtcAddressAndLabelIfInputIsHasValidAddressAndUrlEscapedLabel() {
        ("bitcoin:bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw" +
            "?label=This+is+very+long+comment.+With+1234+and+%21%40%23%24%25%5E%26**%28%29_%2B."
            ).parseBitcoinPaymentInstructions() shouldBe
            BitcoinPaymentInstruction(
                address = "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw",
                label = "This is very long comment. With 1234 and !@#\$%^&**()_+.",
            )
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnBtcAddressAndAmountAndLabelIfInputsAreValid() {
        ("bitcoin:bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw" +
            "?amount=1.234567" +
            "&label=This+is+very+long+comment."
            ).parseBitcoinPaymentInstructions() shouldBe
            BitcoinPaymentInstruction(
                address = "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw",
                amount = "1.234567",
                label = "This is very long comment.",
            )
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldIgnoreUnknownQueryParams() {
        "bitcoin:bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw?priority=FAST&amount=1.23"
            .parseBitcoinPaymentInstructions() shouldBe
            BitcoinPaymentInstruction(
                address = "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw",
                amount = "1.23",
            )
    }

    @Test
    fun parseBitcoinPaymentInstruction_shouldReturnBtcAddressAndLightningAddressAndAmountAndLabelIfInputsAreValid() {
        ("bitcoin:bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw" +
            "?lightning=$validLnInvoice" +
            "&amount=1.234567" +
            "&label=This+is+very+long+comment."
            ).parseBitcoinPaymentInstructions() shouldBe
            BitcoinPaymentInstruction(
                address = "bc1q99ygnq68xrvqd9up7vgapnytwmss4am6ytessw",
                lightning = validLnInvoice,
                amount = "1.234567",
                label = "This is very long comment.",
            )
    }
}
