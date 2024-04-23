package net.primal.android.feed.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteZapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<NoteZapData>)

    @Transaction
    @Query("SELECT * FROM NoteZapData WHERE noteId = :noteId ORDER BY CAST(amountInMillisats AS REAL) DESC LIMIT 10")
    fun observeTopZappers(noteId: String): Flow<List<NoteZap>>
}
