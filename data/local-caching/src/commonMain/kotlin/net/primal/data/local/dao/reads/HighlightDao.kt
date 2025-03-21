package net.primal.data.local.dao.reads

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {
    @Upsert
    suspend fun upsertAll(data: List<HighlightData>)

    @Upsert
    suspend fun upsert(data: HighlightData)

    @Query("DELETE FROM HighlightData WHERE highlightId = :highlightId")
    suspend fun deleteById(highlightId: String)

    @Transaction
    @Query("SELECT * FROM HighlightData WHERE highlightId = :highlightId LIMIT 1")
    fun observeById(highlightId: String): Flow<Highlight?>
}
