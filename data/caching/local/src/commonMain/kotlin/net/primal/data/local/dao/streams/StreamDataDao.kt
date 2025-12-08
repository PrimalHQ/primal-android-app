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

    @Query("SELECT * FROM StreamData WHERE mainHostId IN (:mainHostIds) ORDER BY startsAt DESC")
    suspend fun findStreamData(mainHostIds: List<String>): List<StreamData>

    @Query("SELECT * FROM StreamData WHERE createdAt < (strftime('%s','now') - 43200) AND status = 'live'")
    suspend fun findStaleStreamData(): List<StreamData>

    @Transaction
    @Query("SELECT * FROM StreamData WHERE mainHostId = :mainHostId ORDER BY startsAt DESC")
    fun observeStreamsByMainHostId(mainHostId: String): Flow<List<Stream>>

    @Transaction
    @Query("SELECT * FROM StreamData WHERE aTag = :aTag")
    suspend fun findStreamByATag(aTag: String): Stream?

    @Transaction
    @Query("SELECT * FROM StreamData WHERE aTag = :aTag")
    fun observeStreamByATag(aTag: String): Flow<Stream?>
}
