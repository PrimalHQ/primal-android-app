package net.primal.events.repository

import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import net.primal.core.coroutines.DispatcherProvider
import net.primal.db.PrimalDatabase
import net.primal.events.db.Event

internal class EventRepositoryImpl(
    private val database: PrimalDatabase,
    private val dispatcherProvider: DispatcherProvider,
) : EventRepository {

    override suspend fun insertRandomEvent() =
        withContext(dispatcherProvider.io()) {
            val randomInts = mutableListOf<Int>()
            repeat(times = 3) { randomInts.add(Random.nextInt(from = 0, until = 1_000_000)) }
            database.events().insertEvent(
                Event(
                    relay = "wss://relay.primal.net",
                    raw = randomInts.toString(),
                ),
            )
        }

    override suspend fun insertEvent(event: Event) = database.events().insertEvent(event)

    override suspend fun getAllEvents(): List<Event> = database.events().getAllEvents()

    override suspend fun getAllEvents2(): List<Event> = database.events().getAllEvents()

    override fun observeAllEvents(): Flow<List<Event>> = database.events().observeAllEvents()
}
