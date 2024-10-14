package net.primal.android.explore.home.zaps.ui

import java.time.Instant
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.notes.feed.model.NoteContentUi

data class ExploreZapNoteUi(
    val sender: ProfileDetailsUi?,
    val receiver: ProfileDetailsUi?,
    val amountSats: ULong,
    val zapMessage: String?,
    val createdAt: Instant,
    val noteContentUi: NoteContentUi,
)
