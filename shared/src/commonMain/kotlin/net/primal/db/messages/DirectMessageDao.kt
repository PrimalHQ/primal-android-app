package net.primal.db.messages

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DirectMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<DirectMessageData>)

    @Query("SELECT * FROM DirectMessageData WHERE ownerId = :ownerId ORDER BY createdAt DESC LIMIT 1")
    suspend fun firstByOwnerId(ownerId: String): DirectMessageData?

    @Query(
        """
        SELECT * FROM DirectMessageData 
        WHERE ownerId = :ownerId AND participantId = :participantId
        ORDER BY createdAt DESC LIMIT 1
        """,
    )
    suspend fun firstByOwnerId(ownerId: String, participantId: String): DirectMessageData?

    @Query("SELECT * FROM DirectMessageData WHERE ownerId = :ownerId ORDER BY createdAt ASC LIMIT 1")
    suspend fun lastByOwnerId(ownerId: String): DirectMessageData?

    @Transaction
    @Query(
        """
        SELECT * FROM DirectMessageData
        WHERE ownerId = :ownerId AND participantId = :participantId
        ORDER BY createdAt DESC
    """,
    )
    fun newestMessagesPagedByOwnerId(ownerId: String, participantId: String): PagingSource<Int, DirectMessage>

    @Query("DELETE FROM DirectMessageData WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwnerId(ownerId: String)
}
