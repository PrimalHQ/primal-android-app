package net.primal.data.local.dao.threads

import androidx.room3.Entity

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
