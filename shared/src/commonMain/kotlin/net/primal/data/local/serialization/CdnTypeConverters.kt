package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull
import net.primal.domain.CdnImage
import net.primal.domain.CdnResourceVariant

class CdnTypeConverters {

    @TypeConverter
    fun stringToCdnImage(value: String?): CdnImage? {
        return CommonJson.decodeFromStringOrNull<CdnImage>(value)
    }

    @TypeConverter
    fun cdnImageToString(data: CdnImage?): String? {
        return when (data) {
            null -> null
            else -> CommonJson.encodeToString(data)
        }
    }

    @TypeConverter
    fun stringToListOfCdnResourceVariant(value: String?): List<CdnResourceVariant>? {
        return CommonJson.decodeFromStringOrNull<List<CdnResourceVariant>>(value)
    }

    @TypeConverter
    fun listOfCdnResourceVariantToString(list: List<CdnResourceVariant>?): String? {
        return when (list) {
            null -> null
            else -> CommonJson.encodeToString(list)
        }
    }
}
