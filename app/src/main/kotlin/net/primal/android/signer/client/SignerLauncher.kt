package net.primal.android.signer.client

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.account.signer.local.model.Permission
import net.primal.data.account.signer.local.model.SignerMethod
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent

internal const val AMBER_PACKAGE_NAME = "com.greenart7c3.nostrsigner"
private const val URI_PREFIX = "nostrsigner:"

typealias SignerLauncher = ManagedActivityResultLauncher<Intent, ActivityResult>

@Composable
private fun rememberSignerLauncher(onResult: (ActivityResult) -> Unit): SignerLauncher =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = onResult,
    )

/**
 * Creates a [rememberSignerLauncher] to handle the result of the `get_public_key` flow
 * via a NIP-55 external signer (e.g. Amber, Cambium).
 *
 * When the result is successful ([Activity.RESULT_OK]) and contains a valid public key,
 * [onSuccess] is invoked with the retrieved public key and the package name of the signer
 * that served the request (taken from the `package` result extra, or `null` if the signer
 * did not provide it). Otherwise, [onFailure] is called with the [ActivityResult] to
 * indicate an error state or missing data.
 *
 * @param onSuccess Callback invoked when the public key is successfully retrieved.
 * @param onFailure Callback invoked when the operation fails or no valid public key is found.
 *
 * @return A launcher that can be used within a composable to initiate the public key retrieval flow.
 */
@Composable
fun rememberSignerPubkeyLauncher(
    onFailure: ((ActivityResult) -> Unit)? = null,
    onSuccess: (pubkey: String, signerPackageName: String?) -> Unit,
) = rememberSignerLauncher { result ->
    if (result.resultCode != Activity.RESULT_OK) {
        onFailure?.invoke(result)
        return@rememberSignerLauncher
    }

    val pubkey = result.data?.getStringExtra("result") ?: run {
        onFailure?.invoke(result)
        return@rememberSignerLauncher
    }

    onSuccess(pubkey, result.data?.getStringExtra("package"))
}

/**
 * Creates a [rememberSignerLauncher] for handling the result of a `sign_event` flow
 * via a NIP-55 external signer (e.g. Amber, Cambium).
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
fun rememberSignerSignLauncher(onFailure: ((ActivityResult) -> Unit)? = null, onSuccess: (NostrEvent) -> Unit) =
    rememberSignerLauncher { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            onFailure?.invoke(result)
            return@rememberSignerLauncher
        }

        val nostrEvent = (result.data?.getStringExtra("event"))
            .decodeFromJsonStringOrNull<NostrEvent>()
            ?: run {
                onFailure?.invoke(result)
                return@rememberSignerLauncher
            }

        onSuccess(nostrEvent)
    }

/**
 * Initiates the `get_public_key` flow on a NIP-55 external signer.
 *
 * When called, this function constructs an [Intent] to request a public key from the signer, along
 * with permissions for `nip04_encrypt` and `nip04_decrypt`. To handle the resulting public key (or
 * any errors in the flow), use it together with [rememberSignerPubkeyLauncher].
 *
 * @param signerPackageName The package name of the signer app to use, or `null` to leave the
 * intent unpinned so Android resolves it against all installed NIP-55 signers (showing its
 * disambiguation dialog when there is more than one).
 *
 * @see rememberSignerPubkeyLauncher
 * @throws ActivityNotFoundException if no NIP-55 signer is installed on the device.
 */
@Throws(ActivityNotFoundException::class)
fun SignerLauncher.launchGetPublicKey(signerPackageName: String? = null) {
    val intent = Intent(Intent.ACTION_VIEW, URI_PREFIX.toUri())
    signerPackageName?.let { intent.`package` = it }
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
 * Initiates the `sign_event` flow on a NIP-55 external signer for the provided [NostrUnsignedEvent].
 *
 * This method constructs the appropriate [Intent] with the event data, including the public key,
 * and then launches the signer app to request a signature. To handle the result of this operation
 * (i.e., whether the event was signed or not), pair this function with [rememberSignerSignLauncher].
 *
 * @param event The [NostrUnsignedEvent] that needs to be signed.
 * @param signerPackageName The package name of the signer app the user logged in with.
 *
 * @see rememberSignerSignLauncher
 * @throws ActivityNotFoundException if the signer app is not installed on the device.
 */
@Throws(ActivityNotFoundException::class)
fun SignerLauncher.launchSignEvent(event: NostrUnsignedEvent, signerPackageName: String = AMBER_PACKAGE_NAME) {
    val intent = Intent(Intent.ACTION_VIEW, "$URI_PREFIX${event.encodeToJsonString()}".toUri())
    intent.`package` = signerPackageName

    intent.putExtra("current_user", event.pubKey)
    intent.putExtra("type", SignerMethod.SIGN_EVENT.method)

    launch(intent)
}
