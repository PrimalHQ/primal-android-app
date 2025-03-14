package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.data.serialization.NostrJson
import net.primal.data.serialization.decodeFromStringOrNull
import net.primal.domain.CdnImage
import net.primal.domain.CdnResourceVariant

class CdnTypeConverters {

    @TypeConverter
    fun stringToCdnImage(value: String?): CdnImage? {
        return NostrJson.decodeFromStringOrNull<CdnImage>(value)
    }

    @TypeConverter
    fun cdnImageToString(data: CdnImage?): String? {
        return when (data) {
            null -> null
            else -> NostrJson.encodeToString(data)
        }
    }

    @TypeConverter
    fun stringToListOfCdnResourceVariant(value: String?): List<CdnResourceVariant>? {
        return NostrJson.decodeFromStringOrNull<List<CdnResourceVariant>>(value)
    }

    @TypeConverter
    fun listOfCdnResourceVariantToString(list: List<CdnResourceVariant>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }
}
