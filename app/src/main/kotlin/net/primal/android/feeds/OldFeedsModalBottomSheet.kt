package net.primal.android.feeds

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.ui.FeedList
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Deprecated(message = "Switch to FeedsBottomSheet.")
fun OldFeedsModalBottomSheet(
    title: String,
    feeds: List<FeedUi>,
    activeFeed: FeedUi?,
    onFeedClick: (FeedUi) -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    showAddFeed: Boolean = false,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        contentColor = AppTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        scrimColor = Color.Transparent,
    ) {
        FeedList(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            title = title,
            feeds = feeds,
            activeFeed = activeFeed,
            onFeedClick = onFeedClick,
            onEditFeedClick = {},
            enableEditMode = showAddFeed,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewOldFeedsModalBottomSheet() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        OldFeedsModalBottomSheet(
            title = "Feeds",
            feeds = listOf(
                FeedUi(spec = "1", name = "Test", description = "", specKind = FeedSpecKind.Notes),
                FeedUi(spec = "2", name = "Test Two", description = "", specKind = FeedSpecKind.Notes),
                FeedUi(spec = "3", name = "Test Three", description = "", specKind = FeedSpecKind.Notes),
                FeedUi(spec = "21", name = "Test TwentyOne", description = "", specKind = FeedSpecKind.Notes),
            ),
            activeFeed = FeedUi(spec = "1", name = "Test", description = "", specKind = FeedSpecKind.Notes),
            onFeedClick = {},
            onDismissRequest = {},
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        )
    }
}
