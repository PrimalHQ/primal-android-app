package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.PrimalLegendProfile
import net.primal.domain.PrimalPremiumInfo

class ProfileTypeConverters {

    @TypeConverter
    fun stringToPrimalLegendProfile(value: String?): PrimalLegendProfile? {
        return value.decodeFromJsonStringOrNull<PrimalLegendProfile>()
    }

    @TypeConverter
    fun primalLegendProfileToString(data: PrimalLegendProfile?): String? {
        return when (data) {
            null -> null
            else -> data.encodeToJsonString()
        }
    }

    @TypeConverter
    fun stringToPrimalPremiumInfo(value: String?): PrimalPremiumInfo? {
        return value.decodeFromJsonStringOrNull<PrimalPremiumInfo>()
    }

    @TypeConverter
    fun primalPrimalPremiumInfo(data: PrimalPremiumInfo?): String? {
        return when (data) {
            null -> null
            else -> data.encodeToJsonString()
        }
    }
}
