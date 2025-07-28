package net.primal.domain.streams

enum class StreamStatus(val nostrValue: String) {
    PLANNED("planned"),
    LIVE("live"),
    ENDED("ended"),
    ;

    companion object {
        fun fromString(value: String?): StreamStatus {
            return when (value?.lowercase()) {
                "planned" -> PLANNED
                "live" -> LIVE
                else -> ENDED
            }
        }
    }
}
