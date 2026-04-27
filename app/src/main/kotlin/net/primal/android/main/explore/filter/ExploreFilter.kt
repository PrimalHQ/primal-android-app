package net.primal.android.main.explore.filter

import androidx.annotation.StringRes
import net.primal.android.R

enum class ExploreFilter(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
) {
    Explore(
        titleRes = R.string.explore_filter_explore_title,
        subtitleRes = R.string.explore_filter_explore_subtitle,
    ),
    FeedGallery(
        titleRes = R.string.explore_filter_feed_gallery_title,
        subtitleRes = R.string.explore_filter_feed_gallery_subtitle,
    ),
    FollowPacks(
        titleRes = R.string.explore_filter_follow_packs_title,
        subtitleRes = R.string.explore_filter_follow_packs_subtitle,
    ),
    Zaps(
        titleRes = R.string.explore_filter_zaps_title,
        subtitleRes = R.string.explore_filter_zaps_subtitle,
    ),
    Media(
        titleRes = R.string.explore_filter_media_title,
        subtitleRes = R.string.explore_filter_media_subtitle,
    ),
}
