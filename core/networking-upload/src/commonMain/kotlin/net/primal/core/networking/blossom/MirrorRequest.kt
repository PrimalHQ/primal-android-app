package net.primal.core.networking.blossom

import kotlinx.serialization.Serializable

@Serializable
internal data class MirrorRequest(val url: String)
