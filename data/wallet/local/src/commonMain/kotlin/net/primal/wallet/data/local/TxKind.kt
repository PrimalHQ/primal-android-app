package net.primal.wallet.data.local

/**
 * Transaction kind discriminator for persistence layer.
 * Used to determine which domain Transaction subtype to create when reading from DB.
 */
enum class TxKind {
    LIGHTNING,
    ZAP,
    ON_CHAIN,
    SPARK,
    STORE_PURCHASE,
}
