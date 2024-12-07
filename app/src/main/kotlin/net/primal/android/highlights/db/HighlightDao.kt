package net.primal.android.highlights.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface HighlightDao {
    @Upsert
    fun upsertAll(data: List<HighlightData>)
}
