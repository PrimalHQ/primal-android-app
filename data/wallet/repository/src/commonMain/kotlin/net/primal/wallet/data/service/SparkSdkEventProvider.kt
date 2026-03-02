package net.primal.wallet.data.service

import kotlinx.coroutines.flow.Flow
import net.primal.wallet.data.service.model.SparkSdkEvent

internal interface SparkSdkEventProvider {
    val sdkEvents: Flow<SparkSdkEvent>
}
