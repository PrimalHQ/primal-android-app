package net.primal.android.premium.api.model


data class UpdatePrimalLegendProfileRequest(
    val styleId: String,
    val customBadge: Boolean,
    val avatarGlow: Boolean,
)
