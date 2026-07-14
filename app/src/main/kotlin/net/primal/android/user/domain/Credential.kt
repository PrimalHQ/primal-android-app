package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class Credential(
    val nsec: String?,
    val npub: String,
    val type: CredentialType = CredentialType.PrivateKey,
    val signerPackageName: String? = null,
)

enum class CredentialType {
    InternalSigner,
    ExternalSigner,
    PrivateKey,
    PublicKey,
}
