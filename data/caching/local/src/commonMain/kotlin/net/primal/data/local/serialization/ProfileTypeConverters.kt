package net.primal.data.local.serialization

import androidx.room3.ColumnTypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.membership.PrimalLegendProfile
import net.primal.domain.membership.PrimalPremiumInfo

class ProfileTypeConverters {

    @ColumnTypeConverter
    fun stringToPrimalLegendProfile(value: String?): PrimalLegendProfile? {
        return value.decodeFromJsonStringOrNull<PrimalLegendProfile>()
    }

    @ColumnTypeConverter
    fun primalLegendProfileToString(data: PrimalLegendProfile?): String? {
        return when (data) {
            null -> null
            else -> data.encodeToJsonString()
        }
    }

    @ColumnTypeConverter
    fun stringToPrimalPremiumInfo(value: String?): PrimalPremiumInfo? {
        return value.decodeFromJsonStringOrNull<PrimalPremiumInfo>()
    }

    @ColumnTypeConverter
    fun primalPrimalPremiumInfo(data: PrimalPremiumInfo?): String? {
        return when (data) {
            null -> null
            else -> data.encodeToJsonString()
        }
    }
}
