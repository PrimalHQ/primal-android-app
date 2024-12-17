package net.primal.android.highlights.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface HighlightDao {
    @Upsert
    fun upsertAll(data: List<HighlightData>)

    @Upsert
    fun upsert(data: HighlightData)

    @Query("DELETE FROM HighlightData WHERE highlightId = :highlightId")
    fun deleteById(highlightId: String)
}
