package net.primal.events.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

@Dao
interface EventsDao {

    @NativeCoroutines
    @Upsert
    suspend fun insertEvent(event: Event)

    @NativeCoroutines
    @Query("SELECT * FROM Event")
    fun observeAllEvents(): Flow<List<Event>>

    @NativeCoroutines
    @Query("SELECT * FROM Event")
    suspend fun getAllEvents(): List<Event>

    @NativeCoroutines
    @Query("SELECT * FROM Event WHERE id = :id")
    fun observeEvent(id: Long): Flow<Event?>

    @NativeCoroutines
    @Query("SELECT * FROM Event WHERE id = :id")
    suspend fun getEvent(id: Long): Event?

    @NativeCoroutines
    @Query("DELETE FROM Event WHERE id = :id")
    suspend fun deleteEvent(id: Long)
}
