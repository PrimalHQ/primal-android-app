package net.primal.android.signer

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent

internal const val AMBER_PACKAGE_NAME = "com.greenart7c3.nostrsigner"
private const val URI_PREFIX = "nostrsigner:"

typealias AmberLauncher = ManagedActivityResultLauncher<Intent, ActivityResult>

@Composable
private fun rememberAmberLauncher(onResult: (ActivityResult) -> Unit): AmberLauncher =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = onResult,
    )

/**
 * Creates a [rememberAmberLauncher] to handle the result of the `get_public_key` flow via Amber.
 *
 * When the result is successful ([Activity.RESULT_OK]) and contains a valid public key,
 * [onSuccess] is invoked with the retrieved public key. Otherwise, [onFailure] is called with
 * the [ActivityResult] to indicate an error state or missing data.
 *
 * @param onSuccess Callback invoked when the public key is successfully retrieved.
 * @param onFailure Callback invoked when the operation fails or no valid public key is found.
 *
 * @return A launcher that can be used within a composable to initiate the public key retrieval flow.
 */
@Composable
fun rememberAmberPubkeyLauncher(onFailure: ((ActivityResult) -> Unit)? = null, onSuccess: (pubkey: String) -> Unit) =
    rememberAmberLauncher { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            onFailure?.invoke(result)
            return@rememberAmberLauncher
        }

        val pubkey = result.data?.getStringExtra("result") ?: run {
            onFailure?.invoke(result)
            return@rememberAmberLauncher
        }

        onSuccess(pubkey)
    }

/**
 * Creates a [rememberAmberLauncher] for handling the result of a `sign_event` flow via Amber.
 *
 * This composable function extracts a JSON event from the incoming [ActivityResult], parses it
 * into a [NostrEvent], and then invokes [onSuccess] if the parsing succeeds. If the result isn't successful
 * ([Activity.RESULT_OK]) or if the JSON parsing fails, [onFailure] is called instead.
 *
 * @param onSuccess Callback invoked when the event is successfully parsed into a [NostrEvent].
 * @param onFailure Callback invoked when the operation fails or invalid data is encountered.
 *
 * @return A launcher that can be used within a composable to initiate the `sign_event` flow.
 */
@Composable
fun rememberAmberSignerLauncher(onFailure: ((ActivityResult) -> Unit)? = null, onSuccess: (NostrEvent) -> Unit) =
    rememberAmberLauncher { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            onFailure?.invoke(result)
            return@rememberAmberLauncher
        }

        val nostrEvent = (result.data?.getStringExtra("event"))
            .decodeFromJsonStringOrNull<NostrEvent>()
            ?: run {
                onFailure?.invoke(result)
                return@rememberAmberLauncher
            }

        onSuccess(nostrEvent)
    }

/**
 * Initiates the `get_public_key` flow on the Amber app.
 *
 * When called, this function constructs an [Intent] to request a public key from Amber, along with
 * permissions for `nip04_encrypt` and `nip04_decrypt`. To handle the resulting public key (or any
 * errors in the flow), use it together with [rememberAmberPubkeyLauncher].
 *
 * @see rememberAmberPubkeyLauncher
 */
fun AmberLauncher.launchGetPublicKey() {
    val intent = Intent(Intent.ACTION_VIEW, URI_PREFIX.toUri())
    intent.`package` = AMBER_PACKAGE_NAME
    val permissions = listOf(
        Permission(
            type = SignerMethod.NIP04_ENCRYPT,
        ),
        Permission(
            type = SignerMethod.NIP04_DECRYPT,
        ),
        Permission(
            type = SignerMethod.SIGN_EVENT,
            kind = NostrEventKind.PrimalWalletOperation.value,
        ),
        Permission(
            type = SignerMethod.SIGN_EVENT,
            kind = NostrEventKind.ApplicationSpecificData.value,
        ),
    )

    intent.putExtra("permissions", permissions.encodeToJsonString())
    intent.putExtra("type", SignerMethod.GET_PUBLIC_KEY.method)
    launch(intent)
}

/**
 * Initiates the `sign_event` flow on the Amber app for the provided [NostrUnsignedEvent].
 *
 * This method constructs the appropriate [Intent] with the event data, including the public key,
 * and then launches the Amber app to request a signature. To handle the result of this operation
 * (i.e., whether the event was signed or not), pair this function with [rememberAmberSignerLauncher].
 *
 * @param event The [NostrUnsignedEvent] that needs to be signed.
 *
 * @see rememberAmberSignerLauncher
 */
fun AmberLauncher.launchSignEvent(event: NostrUnsignedEvent) {
    val intent = Intent(Intent.ACTION_VIEW, "$URI_PREFIX${event.encodeToJsonString()}".toUri())
    intent.`package` = AMBER_PACKAGE_NAME

    intent.putExtra("current_user", event.pubKey)
    intent.putExtra("type", SignerMethod.SIGN_EVENT.method)

    launch(intent)
}
