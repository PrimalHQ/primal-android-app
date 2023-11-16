package net.primal.android.theme.domain

enum class PostLayout(
    private val appearanceOptions: List<PostAppearance>,
) {
    Standard(
        appearanceOptions = listOf(
            PostAppearance.StandardSmall,
            PostAppearance.StandardDefault,
            PostAppearance.StandardLarge,
            PostAppearance.StandardExtraLarge,
        ),
    ),
    FullWidth(
        appearanceOptions = listOf(
            PostAppearance.FullWidthSmall,
            PostAppearance.FullWidthDefault,
            PostAppearance.FullWidthLarge,
            PostAppearance.FullWidthExtraLarge,
        ),
    ),
}
