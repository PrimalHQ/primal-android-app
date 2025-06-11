package net.primal.wallet.domain.v2

data class WalletCapabilities(
    val supportsBalance: Boolean,
    val supportsTransactions: Boolean,
    val supportsTxDetails: Boolean,
    val supportsLightning: Boolean,
    val supportsOnChain: Boolean,
    val supportsSendToNostrUser: Boolean,
    val supportsSendToLightningAddress: Boolean,
    val supportsSendToLightningInvoice: Boolean,
    val supportsReceiveOnLightning: Boolean,
    val supportsReceiveOnChain: Boolean,
)
