package net.primal.android.wallet.utils

import android.util.Patterns

fun String.isLnInvoice() = startsWith(prefix = "lnbc", ignoreCase = true)

fun String.isLnUrl() = startsWith(prefix = "lnurl", ignoreCase = true)

fun String.isLightningAddress() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isLightningAddressUri() =
    startsWith(prefix = "lightning:", ignoreCase = true) &&
        Patterns.EMAIL_ADDRESS.matcher(this.split(":").last()).matches()
