package net.primal.wallet.data.remote

enum class WalletOperationVerb(val identifier: String) {
    IS_USER("is_user"),
    BALANCE("balance"),
    DEPOSIT("deposit"),
    WITHDRAW("withdraw"),
    MIGRATION_WITHDRAW("migration_withdraw"),
    TRANSACTIONS("transactions"),
    EXCHANGE_RATE("exchange_rate"),
    USER_INFO("user_info"),
    IN_APP_PURCHASE_QUOTE("in_app_purchase_quote"),
    IN_APP_PURCHASE("in_app_purchase"),
    PARSE_LNURL("parse_lnurl"),
    PARSE_LNINVOICE("parse_lninvoice"),
    ONCHAIN_PAYMENT_TIERS("onchain_payment_tiers"),
    NWC_CONNECTIONS("nwc_connections"),
    NWC_REVOKE_CONNECTION("nwc_revoke"),
    NWC_CREATE_NEW_CONNECTION("nwc_connect"),
    REGISTER_SPARK_PUBKEY("register_spark_pubkey"),
    UNREGISTER_SPARK_PUBKEY("unregister_spark_pubkey"),
}
