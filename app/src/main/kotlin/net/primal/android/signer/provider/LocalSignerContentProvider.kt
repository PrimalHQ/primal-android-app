package net.primal.android.signer.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.primal.android.signer.provider.parser.SignerContentProviderParser
import net.primal.android.signer.provider.utils.getResultString
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.service.LocalSignerError
import net.primal.domain.account.service.LocalSignerService
import timber.log.Timber

class LocalSignerContentProvider : ContentProvider() {

    companion object {
        private const val REJECTED_COLUMN = "rejected"
        private const val EVENT_COLUMN = "event"
        private const val RESULT_COLUMN = "result"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocalSignerContentProviderEntryPoint {
        fun localSignerService(): LocalSignerService
    }

    override fun query(
        p0: Uri,
        p1: Array<out String?>?,
        p2: String?,
        p3: Array<out String?>?,
        p4: String?,
    ): Cursor? {
        return SignerContentProviderParser()
            .parse(uri = p0, params = p1?.filterNotNull() ?: emptyList(), callingPackage = callingPackage)
            .mapCatching {
                Timber.tag("LocalSignerProvider").d("We got $it.")
                val appContext = context?.applicationContext ?: error("Couldn't get application context.")

                val hiltEntryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    LocalSignerContentProviderEntryPoint::class.java,
                )

                val localSignerService = hiltEntryPoint.localSignerService()

                runBlocking(Dispatchers.IO) {
                    withTimeout(2.seconds) {
                        localSignerService.processMethod(method = it).getOrThrow()
                    }
                }
            }
            .fold(
                onSuccess = { response ->
                    Timber.tag("LocalSignerProvider").d("Success! Response: $response")
                    MatrixCursor(response.getColumnNames()).apply {
                        addRow(response.getColumnValues())
                    }
                },
                onFailure = { error ->
                    Timber.tag("LocalSigner").d("Failed to process request: ${error.message}")

                    if (error is LocalSignerError.AutoDenied) {
                        MatrixCursor(arrayOf(REJECTED_COLUMN)).apply {
                            addRow(arrayOf(true.toString()))
                        }
                    } else {
                        null
                    }
                },
            )
    }

    private fun LocalSignerMethodResponse.getColumnNames() =
        when (this) {
            is LocalSignerMethodResponse.Success.SignEvent -> arrayOf(RESULT_COLUMN, EVENT_COLUMN)
            else -> arrayOf(RESULT_COLUMN)
        }

    private fun LocalSignerMethodResponse.getColumnValues() =
        when (this) {
            is LocalSignerMethodResponse.Success.SignEvent -> arrayOf(
                this.getResultString(),
                this.signedEvent.encodeToJsonString(),
            )

            else -> arrayOf(this.getResultString())
        }

    override fun update(
        p0: Uri,
        p1: ContentValues?,
        p2: String?,
        p3: Array<out String?>?,
    ): Int = 0

    override fun delete(
        p0: Uri,
        p1: String?,
        p2: Array<out String?>?,
    ): Int = 0

    override fun getType(p0: Uri): String? = null

    override fun insert(p0: Uri, p1: ContentValues?): Uri? = null

    override fun onCreate(): Boolean = true
}
