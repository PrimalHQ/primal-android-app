package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Upsert
@Dao
interface PostUserStatsDao {

    @Upsert
    fun upsertAll(data: List<PostUserStats>)

}
