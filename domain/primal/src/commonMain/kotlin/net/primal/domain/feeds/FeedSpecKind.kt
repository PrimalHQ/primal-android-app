package net.primal.domain.feeds

enum class FeedSpecKind(val id: String, val settingsKey: String) {
    Reads(id = "reads", settingsKey = "user-reads-feeds"),
    Notes(id = "notes", settingsKey = "user-home-feeds"),
}
