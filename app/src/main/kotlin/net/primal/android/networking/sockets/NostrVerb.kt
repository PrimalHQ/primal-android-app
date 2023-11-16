package net.primal.android.networking.sockets

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class NostrVerb {

    @Serializable
    enum class Outgoing {
        @SerialName("AUTH")
        AUTH,

        @SerialName("CLOSE")
        CLOSE,

        @SerialName("COUNT")
        COUNT,

        @SerialName("EVENT")
        EVENT,

        @SerialName("REQ")
        REQ,
    }

    @Serializable
    enum class Incoming {
        @SerialName("AUTH")
        AUTH,

        @SerialName("COUNT")
        COUNT,

        @SerialName("EOSE")
        EOSE,

        @SerialName("EVENT")
        EVENT,

        @SerialName("NOTICE")
        NOTICE,

        @SerialName("OK")
        OK,
    }
}
