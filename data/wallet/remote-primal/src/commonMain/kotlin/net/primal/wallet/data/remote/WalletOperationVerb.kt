package net.primal.wallet.data.remote

enum class WalletOperationVerb(val identifier: String) {
    EXCHANGE_RATE("exchange_rate"),
    IN_APP_PURCHASE_QUOTE("in_app_purchase_quote"),
    IN_APP_PURCHASE("in_app_purchase"),
    PARSE_LNURL("parse_lnurl"),
    PARSE_LNINVOICE("parse_lninvoice"),
    REGISTER_SPARK_PUBKEY("register_spark_pubkey"),
    UNREGISTER_SPARK_PUBKEY("unregister_spark_pubkey"),
}
