package net.primal.data.local.dao.reads

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
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

    @Query("SELECT * FROM HighlightData WHERE highlightId = :highlightId LIMIT 1")
    suspend fun findById(highlightId: String): Highlight?
}
