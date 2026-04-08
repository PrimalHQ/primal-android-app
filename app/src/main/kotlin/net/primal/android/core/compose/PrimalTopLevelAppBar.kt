package net.primal.android.core.compose

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
    titleOverride: String? = null,
    subtitleOverride: String? = null,
    showTitleChevron: Boolean = false,
    chevronExpanded: Boolean = false,
    onTitleClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val effectiveTitle = titleOverride ?: title
    val effectiveSubtitle = subtitleOverride ?: subtitle
    val effectiveShowChevron = if (titleOverride != null) false else showTitleChevron
    val effectiveOnTitleClick = if (titleOverride != null) null else onTitleClick
    val chevronRotation by animateFloatAsState(
        targetValue = if (chevronExpanded) 180f else 0f,
        label = "ChevronRotation",
    )

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                val titleColumnModifier = if (effectiveOnTitleClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = effectiveOnTitleClick,
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
                            text = effectiveTitle,
                            style = AppTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (effectiveShowChevron) {
                            Icon(
                                modifier = Modifier.rotate(chevronRotation),
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                            )
                        }
                    }
                    if (!effectiveSubtitle.isNullOrBlank()) {
                        Text(
                            text = effectiveSubtitle,
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
