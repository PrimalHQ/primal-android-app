package net.primal.android.editor.domain

import kotlinx.serialization.Serializable

@Serializable
data class NoteTaggedUser(
    val userId: String,
    val userHandle: String,
) {
    val displayUsername get() = "@$userHandle"
}
