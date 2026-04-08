package net.primal.android.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimalTopLevelAppBar(
    title: String,
    avatarCdnImage: CdnImage?,
    avatarLegendaryCustomization: LegendaryCustomization?,
    avatarBlossoms: List<String>,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showTitleChevron: Boolean = false,
    onTitleClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                val titleColumnModifier = if (onTitleClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTitleClick,
                    )
                } else {
                    Modifier
                }

                Column(
                    modifier = titleColumnModifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = AppTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (showTitleChevron) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                            )
                        }
                    }
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            actions = {
                UniversalAvatarThumbnail(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clip(CircleShape),
                    avatarCdnImage = avatarCdnImage,
                    avatarSize = 32.dp,
                    avatarBlossoms = avatarBlossoms,
                    legendaryCustomization = avatarLegendaryCustomization,
                    onClick = onAvatarClick,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppTheme.colorScheme.surface,
                scrolledContainerColor = AppTheme.colorScheme.surface,
            ),
            scrollBehavior = scrollBehavior,
        )

        if (showDivider) {
            PrimalDivider()
        }
    }
}
