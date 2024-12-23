package net.primal.android.thread.articles.details.ui.rendering

import java.net.URL

private val nostrNpub1Regex = Regex("""\bnostr:npub1(\w+)\b""")
private val nostrNote1Regex = Regex("\\b(nostr:|@)((note)1\\w+)\\b|#\\[(\\d+)]")

fun String.replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap: Map<String, String>): String {
    val replacedPart = nostrNpub1Regex.replace(this) { matchResult ->
        val uri = matchResult.groupValues[0]
        val id = "npub1${matchResult.groupValues[1]}"
        val displayName = npubToDisplayNameMap[id] ?: id
        "[$displayName]($uri)"
    }
    return replacedPart
}

fun List<String>.replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap: Map<String, String>) =
    this.map { it.replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap) }

fun String.splitMarkdownByNostrUris(): List<String> {
    val matches = nostrNote1Regex.findAll(this)
    val chunks = mutableListOf<String>()
    var startIndex = 0

    matches.forEach { match ->
        val range = match.range

        if (startIndex < range.first) {
            chunks.add(this.substring(startIndex, range.first).trim())
        }

        chunks.add(this.substring(range).trim())
        startIndex = range.last + 1
    }

    if (startIndex < this.length) {
        chunks.add(this.substring(startIndex).trim())
    }

    return chunks.filter { it.isNotBlank() }
}

fun String.splitMarkdownByInlineImages(): List<String> {
    val regex = Regex("""!\[([^\]]*)\]\(([^)]+)\)""")
    val result = mutableListOf<String>()

    var lastEndIndex = 0

    regex.findAll(this).forEach { match ->
        if (match.range.first > lastEndIndex) {
            result.add(this.substring(lastEndIndex, match.range.first))
        }
        result.add(match.groupValues[2])
        lastEndIndex = match.range.last + 1
    }

    if (lastEndIndex < this.length) {
        result.add(this.substring(lastEndIndex))
    }

    return result
}

fun String.isValidHttpOrHttpsUrl(): Boolean {
    return runCatching {
        val url = URL(this)
        url.protocol == "http" || url.protocol == "https"
    }.getOrNull() == true
}
