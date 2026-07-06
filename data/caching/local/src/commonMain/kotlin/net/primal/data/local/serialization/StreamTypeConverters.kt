package net.primal.data.local.serialization

import androidx.room3.ColumnTypeConverter
import net.primal.domain.streams.StreamStatus

class StreamTypeConverters {

    @ColumnTypeConverter
    fun fromStreamStatus(status: StreamStatus): String {
        return status.nostrValue
    }

    @ColumnTypeConverter
    fun toStreamStatus(value: String): StreamStatus {
        return StreamStatus.fromString(value)
    }
}
