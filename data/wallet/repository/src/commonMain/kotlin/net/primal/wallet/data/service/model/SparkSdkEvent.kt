package net.primal.wallet.data.service.model

import breez_sdk_spark.SdkEvent

internal data class SparkSdkEvent(
    val walletId: String,
    val event: SdkEvent,
)
