package net.primal.android.notes.feed.note.ui.events

import net.primal.android.events.domain.EventUriType

data class MediaClickEvent(
    val noteId: String,
    val eventUriType: EventUriType,
    val mediaUrl: String,
    val positionMs: Long = 0,
)
