package net.primal.android.events.domain

import kotlinx.serialization.Serializable

@Serializable
data class CdnImage(
    val sourceUrl: String,
    val variants: List<CdnResourceVariant> = emptyList(),
)
