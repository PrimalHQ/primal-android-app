package net.primal.android.profile.db.serialization

import androidx.room.TypeConverter
import net.primal.android.profile.domain.PrimalLegendProfile
import net.primal.android.profile.domain.PrimalPremiumInfo
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull

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
