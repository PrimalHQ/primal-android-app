package net.primal.core.networking.nwc.nip47

sealed class NwcMethod(val value: String) {
    object GetBalance : NwcMethod("get_balance")
    object ListTransactions : NwcMethod("list_transactions")
    object MakeInvoice : NwcMethod("make_invoice")
    object LookupInvoice : NwcMethod("lookup_invoice")
    object GetInfo : NwcMethod("get_info")
    object PayInvoice : NwcMethod("pay_invoice")
    object MultiPayInvoice : NwcMethod("multi_pay_invoice")
    object PayKeysend : NwcMethod("pay_keysend")
    object MultiPayKeysend : NwcMethod("multi_pay_keysend")

    // Notification methods
    object PaymentReceived : NwcMethod("payment_received")
    object PaymentSent : NwcMethod("payment_sent")
}
