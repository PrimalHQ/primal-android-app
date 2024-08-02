package net.primal.android.articles.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "spec",
        "articleId",
        "articleAuthorId",
    ],
    indices = [
        Index(value = ["spec"]),
        Index(value = ["articleId"]),
        Index(value = ["articleAuthorId"]),
    ],
)
data class ArticleFeedCrossRef(
    val spec: String,
    val articleId: String,
    val articleAuthorId: String,
)
