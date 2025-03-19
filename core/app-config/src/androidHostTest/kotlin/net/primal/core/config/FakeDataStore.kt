package net.primal.core.config

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

open class FakeDataStore<T>(initialValue: T) : DataStore<T> {

    var latestData: T = initialValue
        private set

    override val data: Flow<T> = flowOf(initialValue)

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        val newData = transform(latestData)
        latestData = newData
        return newData
    }
}
