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
import net.primal.domain.nostr.NostrUnsignedEvent

private const val AMBER_PACKAGE_NAME = "com.greenart7c3.nostrsigner"
private const val URI_PREFIX = "nostrsigner:"

typealias AmberLauncher = ManagedActivityResultLauncher<Intent, ActivityResult>

@Composable
private fun rememberAmberLauncher(onResult: (ActivityResult) -> Unit): AmberLauncher =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = onResult,
    )

/**
 * Assumes `get_public_key` method has been called.  Extracts pubkey from it and
 * allows caller to handle it.  Ignores all failing results.
 */
@Composable
fun rememberAmberPubkeyLauncher(onResult: (pubkey: String) -> Unit) =
    rememberAmberLauncher { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberAmberLauncher
        val pubkey = result.data?.getStringExtra("result") ?: return@rememberAmberLauncher

        onResult(pubkey)
    }

/**
 * Assumes `sign_event` method has been called.  Extracts json event and parses
 * it as NostrEvent giving it to caller to handle.  Ignores all failing results.
 */
@Composable
fun rememberAmberSignerLauncher(onResult: (NostrEvent?) -> Unit) =
    rememberAmberLauncher { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberAmberLauncher

        val nostrEvent = (result.data?.getStringExtra("event"))
            .decodeFromJsonStringOrNull<NostrEvent>()

        onResult(nostrEvent)
    }

/**
 * Launches `get_public_key` method on `AmberLauncher`.  Should be used with
 * `rememberAmberPubkeyLauncher` to handle the result.
 */
fun AmberLauncher.launchGetPublicKey() {
    val intent = Intent(Intent.ACTION_VIEW, URI_PREFIX.toUri())
    intent.`package` = AMBER_PACKAGE_NAME

    intent.putExtra("type", SignerMethod.GET_PUBLIC_KEY.method)
    launch(intent)
}

/**
 * Launches `sign_event` method on `AmberLauncher`.  Should be used with
 * `rememberAmberSignerLauncher` to handle the signed event.
 */
fun AmberLauncher.launchSignEvent(event: NostrUnsignedEvent) {
    val intent = Intent(Intent.ACTION_VIEW, "$URI_PREFIX${event.encodeToJsonString()}".toUri())
    intent.`package` = AMBER_PACKAGE_NAME

    intent.putExtra("current_user", event.pubKey)
    intent.putExtra("type", SignerMethod.SIGN_EVENT.method)

    launch(intent)
}
