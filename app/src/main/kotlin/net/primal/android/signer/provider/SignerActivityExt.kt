package net.primal.android.signer.provider

import androidx.lifecycle.SavedStateHandle
import net.primal.android.navigation.asBase64Decoded
import net.primal.android.navigation.asBase64Encoded
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.account.model.LocalSignerMethod

const val LOCAL_SIGNER_METHOD = "local_signer_method"
fun LocalSignerMethod.asNavArgument(): String = this.encodeToJsonString().asBase64Encoded()
inline val SavedStateHandle.localSignerMethodOrThrow: LocalSignerMethod
    get() = get<String>(LOCAL_SIGNER_METHOD)
        ?.asBase64Decoded()?.decodeFromJsonStringOrNull()
        ?: throw IllegalArgumentException("Missing required $LOCAL_SIGNER_METHOD argument.")
