package net.primal.android.explore.api.model

import java.time.Instant
import net.primal.android.core.compose.profile.model.ProfileDetailsUi

data class ExploreZapData(
    val sender: ProfileDetailsUi?,
    val receiver: ProfileDetailsUi?,
    val amountSats: ULong,
    val zapMessage: String?,
    val noteId: String,
    val noteContent: String?,
    val createdAt: Instant,
)
