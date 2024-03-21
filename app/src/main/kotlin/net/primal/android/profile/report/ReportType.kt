package net.primal.android.profile.report

enum class ReportType(val id: String) {
    Nudity(id = "nudity"),
    Profanity(id = "profanity"),
    Illegal(id = "illegal"),
    Spam(id = "spam"),
    Impersonation(id = "impersonation"),
}
