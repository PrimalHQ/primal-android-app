package net.primal.android.thread.articles.details.ui.rendering

import java.net.URL
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.withNostrPrefix

private val nostrNpub1Regex = Regex("""\b(nostr:)?npub1(\w+)\b""")
private val nostrNprofile1Regex = Regex("""\b(nostr:)?nprofile1(\w+)\b""")
private val nostrNote1Regex = Regex("\\b(nostr:|@)((note)1\\w+)\\b|#\\[(\\d+)]")

fun String.replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap: Map<String, String>): String {
    val replacedPart = nostrNpub1Regex.replace(this) { matchResult ->
        val uri = matchResult.groupValues[0]
        val id = "npub1${matchResult.groupValues[2]}"
        val displayName = npubToDisplayNameMap[id] ?: id
        "[$displayName](${uri.withNostrPrefix()})"
    }.run {
        nostrNprofile1Regex.replace(this) { matchResult ->
            val uri = matchResult.groupValues[0]
            runCatching { uri.extractProfileId()?.hexToNpubHrp() }.getOrNull()?.let { npub ->
                val displayName = npubToDisplayNameMap[npub] ?: npub
                "[$displayName](${npub.withNostrPrefix()})"
            } ?: "[${uri.removePrefix("nostr:")}](${uri.withNostrPrefix()})"
        }
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

/**
 * Uses a regular expression to detect image patterns:
 * - `!\[([^\]]*)\]`        → captures alt text inside square brackets (Group 1)
 * - `\((\S+?)`             → lazily captures the URL (Group 2)
 * - `(?:\s+"[^"]*")?\)`    → optionally matches a quoted image title
 *
 * Example:
 * ```
 * Input:
 *   "Hello ![pic](https://img.com/a.jpg "title") world"
 *
 * Output:
 *   ["Hello ", "https://img.com/a.jpg", " world"]
 * ```
 */
fun String.splitMarkdownByInlineImages(): List<String> {
    val regex = Regex("""!\[([^\]]*)\]\((\S+?)(?:\s+"[^"]*")?\)""")
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
