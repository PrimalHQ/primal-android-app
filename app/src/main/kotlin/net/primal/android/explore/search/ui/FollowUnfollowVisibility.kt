package net.primal.android.explore.search.ui

sealed class FollowUnfollowVisibility {
    data object Visible : FollowUnfollowVisibility()
    data object Invisible : FollowUnfollowVisibility()
    data object Gone : FollowUnfollowVisibility()
}
