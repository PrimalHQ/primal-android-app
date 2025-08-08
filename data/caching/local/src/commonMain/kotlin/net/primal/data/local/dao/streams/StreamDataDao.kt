package net.primal.data.local.dao.streams

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDataDao {

    @Upsert
    suspend fun upsertStreamData(data: List<StreamData>)

    @Transaction
    @Query("SELECT * FROM StreamData WHERE authorId = :authorId ORDER BY startsAt DESC")
    fun observeStreamsByAuthorId(authorId: String): Flow<List<Stream>>

    @Transaction
    @Query("SELECT * FROM StreamData WHERE aTag = :aTag")
    suspend fun findStreamByATag(aTag: String): Stream?

    @Transaction
    @Query("SELECT * FROM StreamData WHERE aTag = :aTag")
    fun observeStreamByATag(aTag: String): Flow<Stream?>
}
