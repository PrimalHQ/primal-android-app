package net.primal.db.profiles.serialization

import androidx.room.TypeConverter
import net.primal.db.profiles.PrimalLegendProfile
import net.primal.db.profiles.PrimalPremiumInfo
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.decodeFromStringOrNull

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

    @TypeConverter
    fun stringToPrimalPremiumInfo(value: String?): PrimalPremiumInfo? {
        return NostrJson.decodeFromStringOrNull<PrimalPremiumInfo>(value)
    }

    @TypeConverter
    fun primalPrimalPremiumInfo(data: PrimalPremiumInfo?): String? {
        return when (data) {
            null -> null
            else -> NostrJson.encodeToString(data)
        }
    }
}
