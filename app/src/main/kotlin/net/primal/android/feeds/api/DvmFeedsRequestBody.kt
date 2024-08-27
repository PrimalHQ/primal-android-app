package net.primal.android.feeds.api

import kotlinx.serialization.Serializable

@Serializable
data class DvmFeedsRequestBody(
    val kind: String,
)
