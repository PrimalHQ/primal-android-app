package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.CdnImage
import net.primal.domain.CdnResourceVariant

class CdnTypeConverters {

    @TypeConverter
    fun stringToCdnImage(value: String?): CdnImage? {
        return value.decodeFromJsonStringOrNull<CdnImage>()
    }

    @TypeConverter
    fun cdnImageToString(data: CdnImage?): String? {
        return when (data) {
            null -> null
            else -> data.encodeToJsonString()
        }
    }

    @TypeConverter
    fun stringToListOfCdnResourceVariant(value: String?): List<CdnResourceVariant>? {
        return value.decodeFromJsonStringOrNull<List<CdnResourceVariant>>()
    }

    @TypeConverter
    fun listOfCdnResourceVariantToString(list: List<CdnResourceVariant>?): String? {
        return when (list) {
            null -> null
            else -> list.encodeToJsonString()
        }
    }
}
