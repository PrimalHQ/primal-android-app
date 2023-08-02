package net.primal.android.profile.details.model

data class ProfileStatsUi(
    val followingCount: Int?,
    val followersCount: Int?,
    val notesCount: Int?,
) { companion object }

fun ProfileStatsUi.Companion.previewExample(): ProfileStatsUi =
    ProfileStatsUi(
        followersCount = 420,
        followingCount = 69,
        notesCount = 1337
    )
