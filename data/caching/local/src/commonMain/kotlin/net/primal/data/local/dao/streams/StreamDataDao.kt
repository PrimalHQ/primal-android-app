package net.primal.data.local.dao.streams

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDataDao {

    @Upsert
    suspend fun upsertStreamData(data: StreamData)

    @Query("SELECT * FROM StreamData WHERE authorId = :authorId")
    fun observeStream(authorId: String): Flow<StreamData?>

    @Query("DELETE FROM StreamData WHERE authorId = :authorId")
    suspend fun deleteStream(authorId: String)
}
