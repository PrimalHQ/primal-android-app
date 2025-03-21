package net.primal.data.local.dao.threads

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
