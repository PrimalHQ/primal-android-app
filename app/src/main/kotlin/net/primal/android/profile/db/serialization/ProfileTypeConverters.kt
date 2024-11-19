package net.primal.android.profile.db.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.profile.domain.PrimalLegendProfile

class ProfileTypeConverters {

    @TypeConverter
    fun stringToPrimalLegendProfile(value: String?): PrimalLegendProfile? {
        return NostrJson.decodeFromStringOrNull<PrimalLegendProfile>(value)
    }

    @TypeConverter
    fun primalLegendProfileToString(data: PrimalLegendProfile?): String? {
        return when (data) {
            null -> null
            else -> NostrJson.encodeToString(data)
        }
    }
}
