package net.primal.android.notes.feed.note.ui.events

import net.primal.domain.links.EventUriType

data class MediaClickEvent(
    val noteId: String,
    val eventUriType: EventUriType,
    val mediaUrl: String,
    val positionMs: Long = 0,
)
