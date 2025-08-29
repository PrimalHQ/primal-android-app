package net.primal.android.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBoltFilled
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamZapLeaderboardBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    bottomSheetHeight: Dp?,
    zaps: List<EventZapUiModel>,
    onZapMessageClick: (EventZapUiModel) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (bottomSheetHeight != null) Modifier.height(bottomSheetHeight) else Modifier),
        ) {
            val totalSats = remember(zaps) { zaps.sumOf { it.amountInSats } }
            val numberFormat = remember { NumberFormat.getNumberInstance() }

            LeaderboardHeader(
                zapCount = zaps.size,
                totalSats = totalSats,
                numberFormat = numberFormat,
            )

            val isDarkTheme = LocalPrimalTheme.current.isDarkTheme
            val listBackgroundColor = if (isDarkTheme) Color.Black else Color.White

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(listBackgroundColor),
                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(
                    items = zaps,
                    key = { it.id },
                ) { zap ->
                    ZapMessageListItem(
                        zap = zap,
                        onClick = { onZapMessageClick(zap) },
                    )
                }
            }
        }
    }
}

private object LeaderboardHeaderDefaults {
    val DarkThemeBackgroundColor = Color(0xFF121212)
    val LightThemeBackgroundColor = Color(0xFFF5F5F5)
    val DarkThemeDividerColor = Color(0xFF222222)
    val LightThemeDividerColor = Color(0xFFE5E5E5)
}

@Composable
private fun LeaderboardHeader(
    zapCount: Int,
    totalSats: ULong,
    numberFormat: NumberFormat,
) {
    val isDarkTheme = LocalPrimalTheme.current.isDarkTheme
    val backgroundColor = if (isDarkTheme) {
        LeaderboardHeaderDefaults.DarkThemeBackgroundColor
    } else {
        LeaderboardHeaderDefaults.LightThemeBackgroundColor
    }
    val dividerColor = if (isDarkTheme) {
        LeaderboardHeaderDefaults.DarkThemeDividerColor
    } else {
        LeaderboardHeaderDefaults.LightThemeDividerColor
    }

    Column(modifier = Modifier.background(backgroundColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ZapCountText(zapCount = zapCount, numberFormat = numberFormat)
            TotalSatsText(totalSats = totalSats, numberFormat = numberFormat)
        }
        PrimalDivider(color = dividerColor)
    }
}

@Composable
private fun ZapCountText(zapCount: Int, numberFormat: NumberFormat) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                ),
            ) {
                append(stringResource(id = R.string.live_stream_leaderboard_total) + " ")
            }
            withStyle(
                style = SpanStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
            ) {
                append(numberFormat.format(zapCount))
            }
            withStyle(
                style = SpanStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                ),
            ) {
                append(" " + stringResource(id = R.string.live_stream_leaderboard_zaps_suffix))
            }
        },
        color = AppTheme.colorScheme.onSurface,
        lineHeight = 20.sp,
    )
}

@Composable
private fun TotalSatsText(totalSats: ULong, numberFormat: NumberFormat) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = PrimalIcons.NavWalletBoltFilled,
            contentDescription = null,
            tint = AppTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = 3.dp),
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(numberFormat.format(totalSats.toLong()))
                }
                withStyle(
                    style = SpanStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    ),
                ) {
                    append(" " + stringResource(id = R.string.live_stream_leaderboard_sats_suffix))
                }
            },
            color = AppTheme.colorScheme.onSurface,
            lineHeight = 20.sp,
        )
    }
}
