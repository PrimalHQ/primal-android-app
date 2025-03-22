package net.primal.android.signer

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.core.utils.serialization.encodeToJsonString

private const val AMBER_PACKAGE_NAME = "com.greenart7c3.nostrsigner"
private const val URI_PREFIX = "nostrsigner:"

typealias AmberLauncher = ManagedActivityResultLauncher<Intent, ActivityResult>

@Composable
fun rememberAmberLauncher(onResult: (ActivityResult) -> Unit): AmberLauncher =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = onResult,
    )

fun AmberLauncher.launchGetPublicKey() {
    val intent = Intent(Intent.ACTION_VIEW, URI_PREFIX.toUri())
    intent.`package` = AMBER_PACKAGE_NAME

    intent.putExtra("type", SignerMethod.GET_PUBLIC_KEY.method)
    launch(intent)
}

fun AmberLauncher.launchSignEvent(event: NostrUnsignedEvent) {
    val intent = Intent(Intent.ACTION_VIEW, "$URI_PREFIX${event.encodeToJsonString()}".toUri())
    intent.`package` = AMBER_PACKAGE_NAME

    intent.putExtra("current_user", event.pubKey)
    intent.putExtra("type", SignerMethod.SIGN_EVENT.method)

    launch(intent)
}
