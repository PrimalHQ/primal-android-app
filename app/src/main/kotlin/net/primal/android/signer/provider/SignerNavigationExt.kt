package net.primal.android.signer.provider

import androidx.lifecycle.SavedStateHandle
import net.primal.android.signer.provider.parser.SignerIntentParser

const val CALLING_PACKAGE = "calling_package"
inline val SavedStateHandle.callingPackageOrThrow: String
    get() = get(CALLING_PACKAGE)
        ?: throw IllegalArgumentException("Missing required calling_package argument.")

inline val SavedStateHandle.signerRequestedPermissionsJsonOrNull: String?
    get() = get(SignerIntentParser.PERMISSIONS_COLUMN)
