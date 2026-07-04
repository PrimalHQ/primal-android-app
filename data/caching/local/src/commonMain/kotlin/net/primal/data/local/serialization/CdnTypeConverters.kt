package net.primal.data.local.serialization

import androidx.room3.ColumnTypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.links.CdnImage
import net.primal.domain.links.CdnResourceVariant

class CdnTypeConverters {

    @ColumnTypeConverter
    fun stringToCdnImage(value: String?): CdnImage? {
        return value.decodeFromJsonStringOrNull<CdnImage>()
    }

    @ColumnTypeConverter
    fun cdnImageToString(data: CdnImage?): String? {
        return when (data) {
            null -> null
            else -> data.encodeToJsonString()
        }
    }

    @ColumnTypeConverter
    fun stringToListOfCdnResourceVariant(value: String?): List<CdnResourceVariant>? {
        return value.decodeFromJsonStringOrNull<List<CdnResourceVariant>>()
    }

    @ColumnTypeConverter
    fun listOfCdnResourceVariantToString(list: List<CdnResourceVariant>?): String? {
        return when (list) {
            null -> null
            else -> list.encodeToJsonString()
        }
    }
}
