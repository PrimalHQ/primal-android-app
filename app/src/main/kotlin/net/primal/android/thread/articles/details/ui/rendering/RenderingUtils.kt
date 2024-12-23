package net.primal.android.thread.articles.details.ui.rendering

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

fun String.splitMarkdownByTables3(): List<String> {
    // Regular expression to match tables and non-table content
    val tablePattern = """([^|]*\n)*(\|[^\n]*\|(?:\n\|[^\n]*\|)+)""".toRegex()

    val chunks = mutableListOf<String>()
    var lastEnd = 0

    // Find all matches
    tablePattern.findAll(this).forEach { match ->
        // Text before the table
        if (match.range.first > lastEnd) {
            chunks.add(this.substring(lastEnd, match.range.first))
        }
        // Table match
        chunks.add(match.value)
        lastEnd = match.range.last + 1
    }

    // Add any remaining text after the last match
    if (lastEnd < this.length) {
        chunks.add(this.substring(lastEnd))
    }

    return chunks
}

fun String.splitIntoParagraphs(): List<String> {
    return this.split(Regex("\\n\\s*\\n")).map { it.trim() }
}
