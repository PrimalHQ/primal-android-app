package net.primal.android.core.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> saveCsvToUri(
    context: Context,
    uri: Uri,
    records: List<T>,
) {
    withContext(Dispatchers.IO) {
        val csv = Csv {
            hasHeaderRecord = true
            recordSeparator = System.lineSeparator()
        }
        val csvContent = csv.encodeToString(records)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(csvContent.toByteArray())
        }
    }
}
