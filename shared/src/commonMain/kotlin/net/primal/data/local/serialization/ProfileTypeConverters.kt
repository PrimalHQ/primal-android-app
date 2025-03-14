package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.data.serialization.NostrJson
import net.primal.data.serialization.decodeFromStringOrNull
import net.primal.domain.PrimalLegendProfile
import net.primal.domain.PrimalPremiumInfo

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
