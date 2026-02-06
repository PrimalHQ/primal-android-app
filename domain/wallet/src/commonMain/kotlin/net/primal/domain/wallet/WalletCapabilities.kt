package net.primal.domain.wallet

data class WalletCapabilities(
    val supportsBalance: Boolean,
    val supportsBalanceSubscription: Boolean,
    val supportsTransactions: Boolean,
    val supportsLightningSend: Boolean,
    val supportsLightningReceive: Boolean,
    val supportsOnChainSend: Boolean,
    val supportsOnChainReceive: Boolean,
    val supportsOnChainFees: Boolean,
    val supportsWalletBackup: Boolean,
    val supportsNwcConnections: Boolean,
)

val Wallet.capabilities: WalletCapabilities
    get() = when (this) {
        is Wallet.Primal -> PrimalCapabilities
        is Wallet.NWC -> NwcCapabilities
        is Wallet.Spark -> SparkCapabilities
    }

private val PrimalCapabilities = WalletCapabilities(
    supportsBalance = true,
    supportsBalanceSubscription = true,
    supportsTransactions = true,
    supportsLightningSend = true,
    supportsLightningReceive = true,
    supportsOnChainSend = true,
    supportsOnChainReceive = true,
    supportsOnChainFees = true,
    supportsWalletBackup = false,
    supportsNwcConnections = true,
)

private val NwcCapabilities = WalletCapabilities(
    supportsBalance = true,
    supportsBalanceSubscription = false,
    supportsTransactions = true,
    supportsLightningSend = true,
    supportsLightningReceive = true,
    supportsOnChainSend = false,
    supportsOnChainReceive = false,
    supportsOnChainFees = false,
    supportsWalletBackup = false,
    supportsNwcConnections = false,
)

private val SparkCapabilities = WalletCapabilities(
    supportsBalance = true,
    supportsBalanceSubscription = false,
    supportsTransactions = true,
    supportsLightningSend = true,
    supportsLightningReceive = true,
    supportsOnChainSend = true,
    supportsOnChainReceive = true,
    supportsOnChainFees = true,
    supportsWalletBackup = true,
    supportsNwcConnections = true,
)
