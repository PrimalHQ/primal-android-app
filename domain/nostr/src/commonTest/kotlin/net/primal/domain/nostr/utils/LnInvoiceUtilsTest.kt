package net.primal.domain.nostr.utils

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.test.assertNull
import net.primal.core.utils.toInt

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

    private val invoiceWithDescription =
        "lnbc420n1p5wuahxpp50qwkd9hn5qdxgeh67ce9qxgdre0vw0rrtr2hnhujp8ahr7rmukuqdq2fah8wctjvscqzzsxqrrs0sp53k2h0f33" +
            "gfpdw3tnm9qr5kelgg6tkh2uknmja3djl5p2gh6kvsjs9qxpqysgqh0m7qkzc0xqm30qdm2arg9qlpkn20gnr49enmfxdjy9qzt6sm" +
            "lwpyt69v6zyjnheyqp8wlh7a3gn53qjzyzs6s44zjpd9sj8dp69uaspam64w2"

    @Test
    fun getAmountInSatsOrNullValidInvoice() {
        val expectedValue = 12345

        val amount = LnInvoiceUtils.getAmountInSatsOrNull(validLnInvoice)

        amount?.toInt() shouldBe expectedValue
    }

    @Test
    fun getAmountInSatsOrNullValidInvoiceNoAmount() {
        val expectedValue = 0

        val amount = LnInvoiceUtils.getAmountInSatsOrNull(validLnInvoiceNoAmount)

        amount?.toInt() shouldBe expectedValue
    }

    @Test
    fun getAmountInSatsOrNullInvalidInvoice() {
        assertNull(LnInvoiceUtils.getAmountInSatsOrNull(invalidLnInvoice))
    }

    @Test
    fun getAmountInSatsOrNullInvalidCharacter() {
        assertNull(LnInvoiceUtils.getAmountInSatsOrNull(invoiceInvalidCharacter))
    }

    @Test
    fun getDescriptionReturnsCorrectValue() {
        val expectedDescription = "Onward"

        val description = LnInvoiceUtils.getDescription(invoiceWithDescription)

        description shouldBe expectedDescription
    }
}
