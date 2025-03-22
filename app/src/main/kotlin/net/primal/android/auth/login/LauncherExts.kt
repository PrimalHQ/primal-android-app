package net.primal.android.auth.login

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.core.net.toUri
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.core.utils.serialization.CommonJson

const val PACKAGE_NAME = "com.greenart7c3.nostrsigner"
const val URI_PREFIX = "nostrsigner:"

enum class SignerMethod(val method: String) {
    GET_PUBLIC_KEY("get_public_key"),
    SIGN_EVENT("sign_event"),
}

typealias SignerLauncher = ManagedActivityResultLauncher<Intent, ActivityResult>

fun SignerLauncher.launchGetPublicKey() {
    val intent = Intent(Intent.ACTION_VIEW, URI_PREFIX.toUri())
    intent.`package` = PACKAGE_NAME

    intent.putExtra("type", SignerMethod.GET_PUBLIC_KEY.method)
    launch(intent)
}

fun SignerLauncher.launchSignEvent(event: NostrUnsignedEvent) {
    val intent = Intent(Intent.ACTION_VIEW, "$URI_PREFIX${CommonJson.encodeToString(event)}".toUri())
    intent.`package` = PACKAGE_NAME

    intent.putExtra("current_user", event.pubKey)
    intent.putExtra("type", SignerMethod.SIGN_EVENT.method)

    launch(intent)
}
