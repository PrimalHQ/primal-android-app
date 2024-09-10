package net.primal.android.notes.db.sql

import androidx.sqlite.db.SimpleSQLiteQuery

interface FeedQueryBuilder {

    fun feedQuery(): SimpleSQLiteQuery

    fun newestFeedPostsQuery(limit: Int): SimpleSQLiteQuery

    fun oldestFeedPostsQuery(limit: Int): SimpleSQLiteQuery
}
