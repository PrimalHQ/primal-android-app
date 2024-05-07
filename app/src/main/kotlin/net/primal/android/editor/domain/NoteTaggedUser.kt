package net.primal.android.editor.domain

data class NoteTaggedUser(
    val userId: String,
    val userHandle: String,
) {
    val displayUsername get() = "@$userHandle"
}
