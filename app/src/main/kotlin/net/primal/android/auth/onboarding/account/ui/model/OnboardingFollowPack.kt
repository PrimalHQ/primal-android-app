package net.primal.android.auth.onboarding.account.ui.model

data class OnboardingFollowPack(
    val name: String,
    val coverUrl: String?,
    val members: List<FollowPackMember>,
)

data class FollowPackMember(
    val userId: String,
    val displayName: String,
    val about: String?,
    val avatarUrl: String?,
)
