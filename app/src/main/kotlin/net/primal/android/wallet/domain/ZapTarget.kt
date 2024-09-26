package net.primal.android.wallet.domain

sealed class ZapTarget {
    data class Profile(
        val pubkey: String,
        val lnUrlDecoded: String,
    ) : ZapTarget()

    data class Note(
        val id: String,
        val authorPubkey: String,
        val authorLnUrlDecoded: String,
    ) : ZapTarget()

    data class Article(
        val eventId: String,
        val articleId: String,
        val eventAuthorId: String,
        val eventAuthorLnUrlDecoded: String,
    ) : ZapTarget()
}

fun ZapTarget.userId(): String {
    return when (this) {
        is ZapTarget.Note -> this.authorPubkey
        is ZapTarget.Profile -> this.pubkey
        is ZapTarget.Article -> this.eventAuthorId
    }
}

fun ZapTarget.lnUrlDecoded(): String {
    return when (this) {
        is ZapTarget.Note -> this.authorLnUrlDecoded
        is ZapTarget.Profile -> this.lnUrlDecoded
        is ZapTarget.Article -> this.eventAuthorLnUrlDecoded
    }
}
