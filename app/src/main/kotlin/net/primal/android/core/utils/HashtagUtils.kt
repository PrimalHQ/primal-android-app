package net.primal.android.core.utils

import net.primal.android.nostr.ext.getTagValueOrNull
import net.primal.android.nostr.ext.isHashtagTag
import net.primal.android.nostr.model.NostrEvent

private val hashtagRegex = Regex("#\\w+")

private val nip08MentionRegex = Regex("\\#\\[([0-9]*)\\]")

private fun String.parseHashtags(regex: Regex): Set<String> {
    return regex.findAll(this)
        .map { it.value.trim() }
        .toSet()
}

fun String.parseHashtags(): List<String> {
    val hashtags = this.parseHashtags(hashtagRegex)
    val nip08MentionHashtags = this.parseHashtags(nip08MentionRegex)
    return (hashtags - nip08MentionHashtags).toList()
}

fun NostrEvent.parseHashtags(): List<String> {
    val hashtags = this.content.parseHashtags(hashtagRegex)
    val nip08MentionHashtags = this.content.parseHashtags(nip08MentionRegex)
    val hashtagsFromTags = this.tags
        ?.filter { it.isHashtagTag() }
        ?.mapNotNull { it.getTagValueOrNull() }
        ?.map { "#$it" }
        ?.toSet() ?: emptySet()

    val allTags = (hashtags - nip08MentionHashtags) + hashtagsFromTags
    return allTags.toList()
}
