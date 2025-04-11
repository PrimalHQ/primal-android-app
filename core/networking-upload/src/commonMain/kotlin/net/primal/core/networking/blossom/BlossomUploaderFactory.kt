package net.primal.core.networking.blossom

import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

object BlossomUploaderFactory {

    fun create(signatureHandler: NostrEventSignatureHandler): BlossomUploader {
        return BlossomUploaderImpl(
            signatureHandler = signatureHandler,
        )
    }
}
