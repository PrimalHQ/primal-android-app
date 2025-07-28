package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.domain.streams.StreamStatus

class StreamTypeConverters {

    @TypeConverter
    fun fromStreamStatus(status: StreamStatus): String {
        return status.nostrValue
    }

    @TypeConverter
    fun toStreamStatus(value: String): StreamStatus {
        return StreamStatus.fromString(value)
    }
}
