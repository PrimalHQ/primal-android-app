package net.primal.android.editor.domain

data class PollPublishRequest(
    val isZapPoll: Boolean,
    val choices: List<PollOption>,
    val endsAt: Long,
    val minZapAmountInSats: Long? = null,
    val maxZapAmountInSats: Long? = null,
)

data class PollOption(
    val id: String,
    val label: String,
)
