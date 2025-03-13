package net.primal.android.highlights.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Upsert
    fun upsertAll(data: List<HighlightData>)

    @Upsert
    fun upsert(data: HighlightData)

    @Query("DELETE FROM HighlightData WHERE highlightId = :highlightId")
    fun deleteById(highlightId: String)

    @Transaction
    @Query("SELECT * FROM HighlightData WHERE highlightId = :highlightId LIMIT 1")
    fun observeById(highlightId: String): Flow<Highlight?>
}
