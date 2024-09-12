package net.primal.android.wallet.activation.regions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RegionSelectionBottomSheet(
    regions: List<Region>,
    title: String,
    onRegionClick: (Region) -> Unit,
    onDismissRequest: () -> Unit,
    skipPartiallyExpanded: Boolean = true,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    ModalBottomSheet(
        modifier = Modifier.padding(top = 32.dp),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        scrimColor = scrimColor,
        contentWindowInsets = {
            WindowInsets(
                left = 0,
                top = BottomSheetDefaults.windowInsets.getTop(LocalDensity.current),
                right = 0,
                bottom = 0,
            )
        },
    ) {
        Surface(
            color = AppTheme.extraColorScheme.surfaceVariantAlt2,
            contentColor = AppTheme.colorScheme.onSurfaceVariant,
        ) {
            LazyColumn(
                modifier = Modifier.navigationBarsPadding(),
            ) {
                stickyHeader {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                        ),
                        title = { Text(text = title) },
                    )
                }

                items(
                    items = regions,
                    key = { it.code },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onRegionClick(it)
                                onDismissRequest()
                            },
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .fillMaxWidth(),
                            text = it.name,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
