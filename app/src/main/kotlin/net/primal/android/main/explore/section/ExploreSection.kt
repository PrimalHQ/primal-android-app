package net.primal.android.main.explore.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.AppBarPage

enum class ExploreSection {
    Explore,
    FeedGallery,
    FollowPacks,
    Zaps,
    Media,
}

@Composable
fun ExploreSection.toTitle(): String =
    when (this) {
        ExploreSection.Explore -> stringResource(id = R.string.explore_section_explore_title)
        ExploreSection.FeedGallery -> stringResource(id = R.string.explore_section_feed_gallery_title)
        ExploreSection.FollowPacks -> stringResource(id = R.string.explore_section_follow_packs_title)
        ExploreSection.Zaps -> stringResource(id = R.string.explore_section_zaps_title)
        ExploreSection.Media -> stringResource(id = R.string.explore_section_media_title)
    }

@Composable
fun ExploreSection.toSubtitle(): String =
    when (this) {
        ExploreSection.Explore -> stringResource(id = R.string.explore_section_explore_subtitle)
        ExploreSection.FeedGallery -> stringResource(id = R.string.explore_section_feed_gallery_subtitle)
        ExploreSection.FollowPacks -> stringResource(id = R.string.explore_section_follow_packs_subtitle)
        ExploreSection.Zaps -> stringResource(id = R.string.explore_section_zaps_subtitle)
        ExploreSection.Media -> stringResource(id = R.string.explore_section_media_subtitle)
    }

@Composable
fun List<ExploreSection>.toAppBarPages(): List<AppBarPage> =
    map { AppBarPage(title = it.toTitle(), subtitle = it.toSubtitle()) }
