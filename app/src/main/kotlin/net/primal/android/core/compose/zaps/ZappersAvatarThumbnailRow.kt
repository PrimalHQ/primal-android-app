package net.primal.android.core.compose.zaps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import java.util.*
import kotlinx.datetime.Clock
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.note.ui.EventZapUiModel
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun ZappersAvatarThumbnailRow(zaps: List<EventZapUiModel>) {
    Box(
        modifier = Modifier,
        contentAlignment = Alignment.CenterEnd,
    ) {
        val reversed = zaps.reversed()
        repeat(times = zaps.size) { index ->
            val zap = reversed[index]
            key(zap.id) {
                AvatarThumbnail(
                    modifier = Modifier.padding(end = index.times(18.dp)),
                    avatarSize = 24.dp,
                    avatarCdnImage = zap.zapperAvatarCdnImage,
                    hasBorder = true,
                    borderSize = 1.dp,
                    borderColor = AppTheme.colorScheme.surface,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewZappersAvatarThumbnailRow() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                ZappersAvatarThumbnailRow(
                    zaps = listOf(
                        EventZapUiModel(
                            id = UUID.randomUUID().toString(),
                            amountInSats = 200L.toULong(),
                            message = "",
                            zappedAt = Clock.System.now().toEpochMilliseconds(),
                            zapperHandle = "zapper",
                            zapperId = "zapperId",
                            zapperName = "Zapper",
                        ),
                        EventZapUiModel(
                            id = UUID.randomUUID().toString(),
                            amountInSats = 200L.toULong(),
                            message = "",
                            zappedAt = Clock.System.now().toEpochMilliseconds(),
                            zapperHandle = "zapper",
                            zapperId = "zapperId",
                            zapperName = "Zapper",
                        ),
                        EventZapUiModel(
                            id = UUID.randomUUID().toString(),
                            amountInSats = 200L.toULong(),
                            message = "",
                            zappedAt = Clock.System.now().toEpochMilliseconds(),
                            zapperHandle = "zapper",
                            zapperId = "zapperId",
                            zapperName = "Zapper",
                        ),
                    ),
                )
            }
        }
    }
}
