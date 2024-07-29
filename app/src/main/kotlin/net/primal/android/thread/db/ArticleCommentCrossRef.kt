package net.primal.android.thread.db

import androidx.room.Entity

@Entity(
    primaryKeys = [
        "articleId",
        "articleAuthorId",
        "commentNoteId",
    ],
)
data class ArticleCommentCrossRef(
    val articleId: String,
    val articleAuthorId: String,
    val commentNoteId: String,
)
