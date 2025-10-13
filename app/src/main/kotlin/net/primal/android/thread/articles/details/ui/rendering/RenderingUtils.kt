package net.primal.android.thread.articles.details.ui.rendering

import java.net.URL
import net.primal.android.thread.articles.details.ui.model.ArticleContentSegment
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.withNostrPrefix

private const val LINKED_IMAGE_URL_GROUP_INDEX = 1
private const val LINK_URL_GROUP_INDEX = 2
private const val SIMPLE_IMAGE_URL_GROUP_INDEX = 3
private val nostrNpub1Regex = Regex("""\b(nostr:)?npub1(\w+)\b""")
private val nostrNprofile1Regex = Regex("""\b(nostr:)?nprofile1(\w+)\b""")
private val nostrNote1Regex = Regex("\\b(nostr:|@)((note)1\\w+)\\b|#\\[(\\d+)]")
private val rawImageUrlRegex =
    Regex("""(https?://\S+\.(?:png|jpg|jpeg|gif|webp|avif))""", RegexOption.IGNORE_CASE)

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

fun List<ArticleContentSegment>.replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap: Map<String, String>) =
    this.map {
        when (it) {
            is ArticleContentSegment.Text -> ArticleContentSegment.Text(
                content = it.content.replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap),
            )
            is ArticleContentSegment.Media -> it
        }
    }

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
 * - **Group 2**: URL of the link wrapping the image.
 * - **Group 3**: URL of a standalone image.
 */
fun String.splitMarkdownByInlineImages(): List<ArticleContentSegment> {
    val imageRegex =
        Regex("""\[!\[[^]]*]\(([^\s")]+)(?:\s+"[^"]*")?\)\]\(([^\s")]+)\)|!\[[^]]*]\(([^\s")]+)(?:\s+"[^"]*")?\)""")

    val result = mutableListOf<ArticleContentSegment>()
    var lastEndIndex = 0

    imageRegex.findAll(this).forEach { match ->
        if (match.range.first > lastEndIndex) {
            result.add(ArticleContentSegment.Text(this.substring(lastEndIndex, match.range.first)))
        }

        val linkedImageUrl = match.groups[LINKED_IMAGE_URL_GROUP_INDEX]?.value
        val linkUrl = match.groups[LINK_URL_GROUP_INDEX]?.value
        val simpleImageUrl = match.groups[SIMPLE_IMAGE_URL_GROUP_INDEX]?.value

        if (linkedImageUrl != null && linkUrl != null) {
            result.add(ArticleContentSegment.Media(mediaUrl = linkedImageUrl, linkUrl = linkUrl))
        } else if (simpleImageUrl != null) {
            result.add(ArticleContentSegment.Media(mediaUrl = simpleImageUrl))
        }

        lastEndIndex = match.range.last + 1
    }

    if (lastEndIndex < this.length) {
        result.add(ArticleContentSegment.Text(this.substring(lastEndIndex)))
    }

    return result
}

fun String.isValidHttpOrHttpsUrl(): Boolean {
    return runCatching {
        val url = URL(this)
        url.protocol == "http" || url.protocol == "https"
    }.getOrNull() == true
}

fun String.splitByRawImageUrls(): List<ArticleContentSegment> {
    val segments = mutableListOf<ArticleContentSegment>()
    var lastIndex = 0

    rawImageUrlRegex.findAll(this).forEach { matchResult ->
        if (matchResult.range.first > lastIndex) {
            val textSegment = this.substring(lastIndex, matchResult.range.first)
            if (textSegment.isNotBlank()) {
                segments.add(ArticleContentSegment.Text(textSegment))
            }
        }

        val imageUrl = matchResult.value
        segments.add(ArticleContentSegment.Media(mediaUrl = imageUrl))

        lastIndex = matchResult.range.last + 1
    }

    if (lastIndex < this.length) {
        val remainingText = this.substring(lastIndex)
        if (remainingText.isNotBlank()) {
            segments.add(ArticleContentSegment.Text(remainingText))
        }
    }

    if (segments.isEmpty() && this.isNotBlank()) {
        segments.add(ArticleContentSegment.Text(this))
    }

    return segments
}
