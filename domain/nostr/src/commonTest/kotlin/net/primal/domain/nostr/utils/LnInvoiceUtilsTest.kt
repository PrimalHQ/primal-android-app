package net.primal.domain.nostr.utils

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.test.assertFailsWith

class LnInvoiceUtilsTest {

    private val validLnInvoice =
        "lnbc123450n1pj7welppp53umfyxp6jn9uvkt463hydtq2zpfvz78hxhpfv9wqx6v4uwdw2rnqdzqg3hkuct5v56zu3n4dcsxgmmwv96x" +
            "jmmwyp6x7gzqgpc8ymmrv4h8ghmrwfuhqar0cqzpgxqrrsssp5tntqjpngx6l8y9va9tzd7fmtemtyp5vvsqphw8f8yqjjrr26x5qs9" +
            "qyyssqyyv7tqp5kpsmv6s5825kcq8fxsn4ag2h5uj2j6lnsnclyyq6844khayzqrl7yue46nwlukfr4uftqcwzxzh8krqg9rqsg9tg6x" +
            "ggszcp0gyjcd"

    private val invoiceInvalidCharacter =
        "lnbc123450n1pj7welpp 53umfyxp6jn9uvkt463hydtq2zpfvz78hxhpfv9wqx6v4uwdw2rnqdzqg3hkuct5v56zu3n4dcsxgmmwv96x" +
            "jmmwyp6x7gzqgpc8ymmrv4h8ghmrwfuhqar0cqzpgxqrrsssp5tntqjpngx6l8y9va9tzd7fmtemtyp5vvsqphw8f8yqjjrr26x5qs9" +
            "qyyssqyyv7tqp5kpsmv6s5825kcq8fxsn4ag2h5uj2j6lnsnclyyq6844khayzqrl7yue46nwlukfr4uftqcwzxzh8krqg9rqsg9tg6" +
            "xggszcp0gyjcd"

    private val invalidLnInvoice =
        "lnbc123450n1pj7welppp53umfyxp6jn9uvkt463hydtq2zpfvz78hxhpfv9wqx6v4uwdw2rnqdzqg3hkuct5v56zu3n4dcsxgmmwv96x" +
            "jmmwyp6x7gzqgpc8ymmrv4h8ghmrwXuhqar0cqzpgxqrrsssp5tntqjpngx6l8y9va9tzd7fmtemtyp5vvsqphw8f8yqjjrr26x5qs9" +
            "qyyssqyyv7tqp5kpsmv6s5825kcq8fxsn4ag2h5uj2j6lnsnclyyq6844khayzqrl7yue46nwlukfr4uftqcwzxzh8krqg9rqsg9tg6x" +
            "ggszcp0gyjcd"

    private val validLnInvoiceNoAmount =
        "lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g" +
            "6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k6" +
            "3n7erqz25le42c4u4ecky03ylcqca784w"

    @Test
    fun getAmountInSatsValidInvoice() {
        val expectedValue = 12345

        val amount = LnInvoiceUtils.getAmountInSats(validLnInvoice)

        amount.intValue() shouldBe expectedValue
    }

    @Test
    fun getAmountInSatsValidInvoiceNoAmount() {
        val expectedValue = 0

        val amount = LnInvoiceUtils.getAmountInSats(validLnInvoiceNoAmount)

        amount.intValue() shouldBe expectedValue
    }

    @Test
    fun getAmountInSatsInvalidInvoice() {
        assertFailsWith(IllegalArgumentException::class) {
            LnInvoiceUtils.getAmountInSats(invalidLnInvoice)
        }
    }

    @Test
    fun getAmountInSatsInvalidCharacter() {
        assertFailsWith(IllegalArgumentException::class) {
            LnInvoiceUtils.getAmountInSats(invoiceInvalidCharacter)
        }
    }
}
