package net.primal.data.local.queries

import androidx.room.RoomRawQuery

interface FeedQueryBuilder {

    fun feedQuery(): RoomRawQuery

    fun newestFeedPostsQuery(limit: Int): RoomRawQuery

    fun oldestFeedPostsQuery(limit: Int): RoomRawQuery
}
