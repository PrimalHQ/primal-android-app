package net.primal.android.profile.db.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.domain.PrimalLegendProfile
import net.primal.domain.PrimalPremiumInfo

class ProfileTypeConverters {

    @TypeConverter
    fun stringToPrimalLegendProfile(value: String?): PrimalLegendProfile? {
        return CommonJson.decodeFromStringOrNull<PrimalLegendProfile>(value)
    }

    @TypeConverter
    fun primalLegendProfileToString(data: PrimalLegendProfile?): String? {
        return when (data) {
            null -> null
            else -> CommonJson.encodeToString(data)
        }
    }

    @TypeConverter
    fun stringToPrimalPremiumInfo(value: String?): PrimalPremiumInfo? {
        return CommonJson.decodeFromStringOrNull<PrimalPremiumInfo>(value)
    }

    @TypeConverter
    fun primalPrimalPremiumInfo(data: PrimalPremiumInfo?): String? {
        return when (data) {
            null -> null
            else -> CommonJson.encodeToString(data)
        }
    }
}
