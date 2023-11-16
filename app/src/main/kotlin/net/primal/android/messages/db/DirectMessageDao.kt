package net.primal.android.messages.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DirectMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<DirectMessageData>)

    @Query("SELECT * FROM DirectMessageData ORDER BY createdAt DESC LIMIT 1")
    fun first(): DirectMessageData?

    @Query("SELECT * FROM DirectMessageData WHERE participantId = :participantId ORDER BY createdAt DESC LIMIT 1")
    fun first(participantId: String): DirectMessageData?

    @Query("SELECT * FROM DirectMessageData ORDER BY createdAt ASC LIMIT 1")
    fun last(): DirectMessageData?

    @Query("SELECT * FROM DirectMessageData WHERE participantId = :participantId ORDER BY createdAt DESC")
    fun newestMessagesPaged(participantId: String): PagingSource<Int, DirectMessage>
}
