package net.primal.shared.data.local.encryption

import kotlin.jvm.JvmInline


@JvmInline
value class EncryptableLong(val decrypted: Long)

fun Long.asEncryptable(): EncryptableLong = EncryptableLong(decrypted = this)
