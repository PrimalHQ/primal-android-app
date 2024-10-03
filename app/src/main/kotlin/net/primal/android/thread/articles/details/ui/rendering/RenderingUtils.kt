package net.primal.android.thread.articles.details.ui.rendering

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

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

fun String.splitIntoParagraphs(): List<String> {
    return this.split(Regex("\\n\\s*\\n")).map { it.trim() }
}

private fun String.markdownToHtml(): String {
    val flavour = CommonMarkFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(this)
    val htmlContent = HtmlGenerator(this, parsedTree, flavour).generateHtml()
    return ARTICLE_BASE_HTML.replace(
        oldValue = "{{ CONTENT }}",
        newValue = htmlContent.substring(startIndex = 6, endIndex = htmlContent.length - 7),
    )
}
