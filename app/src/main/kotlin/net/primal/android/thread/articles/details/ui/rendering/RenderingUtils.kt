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
 * Uses a regular expression to detect and split by both simple and linked Markdown images.
 * - Handles simple images: `![](url)` and `![](url "title")`
 * - Handles linked images: `[![](url)](link)` and `[![](url "title")](link)`
 *
 * Captures:
 * - **Group 1**: URL of the inner image when wrapped in a link.
 * - **Group 2**: URL of a standalone image.
 *
 * Example:
 * ```
 * Input:
 *   "See linked: [![](https://img.com/a.png)](link) and simple: ![pic](https://img.com/b.jpg)"
 *
 * Output:
 *   ["See linked: ", "https://img.com/a.png", " and simple: ", "https://img.com/b.jpg", ""]
 * ```
 */
fun String.splitMarkdownByInlineImages(): List<String> {
    val imageRegex =
        Regex("""\[!\[[^]]*]\(([^\s")]+)(?:\s+"[^"]*")?\)\]\([^\s")]+\)|!\[[^]]*]\(([^\s")]+)(?:\s+"[^"]*")?\)""")

    val result = mutableListOf<String>()
    var lastEndIndex = 0

    imageRegex.findAll(this).forEach { match ->
        if (match.range.first > lastEndIndex) {
            result.add(this.substring(lastEndIndex, match.range.first))
        }

        val url = match.groups[1]?.value ?: match.groups[2]?.value
        if (url != null) {
            result.add(url)
        }

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
