package net.primal.android.core.compose.note

data class TaggedUser(
    val userId: String,
    val userHandle: String,
) {
    val displayUsername get() = "@$userHandle"
}
