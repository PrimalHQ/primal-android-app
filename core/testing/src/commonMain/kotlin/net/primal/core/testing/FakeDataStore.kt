package net.primal.core.testing

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

open class FakeDataStore<T>(initialValue: T) : DataStore<T> {

    private val _data = MutableStateFlow(initialValue)

    val latestData: T get() = _data.value

    override val data: Flow<T> = _data

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        val newData = transform(_data.value)
        _data.value = newData
        return newData
    }
}
