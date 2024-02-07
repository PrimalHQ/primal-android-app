package net.primal.android.wallet.api.model

enum class WalletOperationVerb(val identifier: String) {
    IS_USER("is_user"),
    BALANCE("balance"),
    DEPOSIT("deposit"),
    WITHDRAW("withdraw"),
    TRANSACTIONS("transactions"),
    EXCHANGE_RATE("exchange_rate"),
    USER_INFO("user_info"),
    IN_APP_PURCHASE_QUOTE("in_app_purchase_quote"),
    IN_APP_PURCHASE("in_app_purchase"),
    GET_ACTIVATION_CODE("get_activation_code"),
    ACTIVATE("activate"),
    PARSE_LNURL("parse_lnurl"),
    PARSE_LNINVOICE("parse_lninvoice"),
    ONCHAIN_PAYMENT_TIERS("onchain_payment_tiers"),
}
