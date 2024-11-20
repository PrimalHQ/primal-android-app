package net.primal.android.premium.manage.content.model

data class ContentType(
    val group: ContentGroup,
    val count: Long? = null,
    val broadcasting: Boolean = false,
    val progress: Float = 0.0f,
)
