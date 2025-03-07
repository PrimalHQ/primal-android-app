package net.primal.events.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import net.primal.events.db.Event

interface EventRepository {
    @NativeCoroutines
    suspend fun insertRandomEvent()

    @NativeCoroutines
    suspend fun insertEvent(event: Event)

    @NativeCoroutines
    suspend fun getAllEvents(): List<Event>

    @NativeCoroutines
    suspend fun getAllEvents2(): List<Event>

    @NativeCoroutines
    fun observeAllEvents(): Flow<List<Event>>
}
