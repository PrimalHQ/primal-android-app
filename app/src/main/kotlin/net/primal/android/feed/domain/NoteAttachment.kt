package net.primal.android.feed.domain

import android.net.Uri
import java.util.UUID

data class NoteAttachment(
    val id: UUID = UUID.randomUUID(),
    val localUri: Uri,
    val remoteUrl: String? = null,
    val uploadError: Throwable? = null,
)
