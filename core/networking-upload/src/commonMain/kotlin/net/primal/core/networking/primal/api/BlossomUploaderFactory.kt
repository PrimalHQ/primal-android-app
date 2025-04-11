package net.primal.core.networking.primal.api

import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

object BlossomUploaderFactory {

    fun create(signatureHandler: NostrEventSignatureHandler): BlossomUploader {
        return BlossomUploaderImpl(
            signatureHandler = signatureHandler,
        )
    }
}
