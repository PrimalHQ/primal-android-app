package net.primal.wallet.data.nwc

import net.primal.core.networking.nwc.nip47.NwcMethod

/**
 * Defines the NWC wallet service capabilities per NIP-47.
 * Single source of truth for supported methods, encryption, notifications and other info.
 */
object NwcCapabilities {

    /**
     * Supported NWC methods that this wallet service can handle.
     */
    val supportedMethods: List<String> = listOf(
        NwcMethod.GetInfo.value,
        NwcMethod.GetBalance.value,
        NwcMethod.PayInvoice.value,
        NwcMethod.MakeInvoice.value,
        NwcMethod.LookupInvoice.value,
        NwcMethod.ListTransactions.value,
    )

    /**
     * Supported encryption schemes for NWC communication.
     */
    val supportedEncryption: List<String> = listOf(
        "nip04",
    )

    /**
     * Supported notification types that this wallet service can send.
     */
    val supportedNotifications: List<String> = emptyList()

    /**
     * Network on which we operate. Can be "mainnet", "testnet", "signet" or "regtest"
     */
    const val NETWORK: String = "mainnet"
}
