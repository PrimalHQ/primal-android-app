package net.primal.android.profile.domain

enum class ProfileFeedDirective(val prefix: String) {
    AuthoredNotes(prefix = "authored"),
    AuthoredReplies(prefix = "authoredreplies"),
}
