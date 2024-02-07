package net.primal.android.wallet.transactions.send.prepare.domain

enum class RecipientType {
    LnInvoice,
    LnUrl,
    LightningAddress,
    BitcoinAddress,
}
