package net.primal.android.core.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgeAqua
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgeBlue
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgeBrown
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgeGold
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgePurple
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgePurpleHaze
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgeSilver
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgeSunFire
import net.primal.android.core.compose.icons.primaliconpack.PrimalBadgeTeal
import net.primal.android.core.compose.icons.primaliconpack.Verified
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.isPrimalIdentifier
import net.primal.android.premium.legend.LegendaryProfile
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun NostrUserText(
    displayName: String,
    internetIdentifier: String?,
    modifier: Modifier = Modifier,
    displayNameColor: Color = AppTheme.colorScheme.onSurface,
    fontSize: TextUnit = TextUnit.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = 1,
    internetIdentifierBadgeSize: Dp = 14.dp,
    internetIdentifierBadgeAlign: PlaceholderVerticalAlign = PlaceholderVerticalAlign.Center,
    customBadge: LegendaryProfile? = null,
    annotatedStringPrefixBuilder: (AnnotatedString.Builder.() -> Unit)? = null,
    annotatedStringSuffixBuilder: (AnnotatedString.Builder.() -> Unit)? = null,
) {
    val verifiedBadge = !internetIdentifier.isNullOrEmpty()

    val titleText = buildAnnotatedString {
        annotatedStringPrefixBuilder?.invoke(this)
        append(
            AnnotatedString(
                text = displayName,
                spanStyle = SpanStyle(
                    color = displayNameColor,
                    fontStyle = style.fontStyle,
                    fontWeight = FontWeight.Bold,
                ),
            ),
        )
        if (verifiedBadge) {
            append(' ')
            appendInlineContent("verifiedBadge", "[badge]")
            append(' ')
        }
        annotatedStringSuffixBuilder?.invoke(this)
    }

    val placeholderSize = internetIdentifierBadgeSize.value.sp
    val inlineContent = mapOf(
        "verifiedBadge" to InlineTextContent(
            placeholder = Placeholder(placeholderSize, placeholderSize, internetIdentifierBadgeAlign),
        ) {
            if (customBadge != null && customBadge != LegendaryProfile.NO_CUSTOMIZATION) {
                @Suppress("KotlinConstantConditions")
                val badgeVector = when (customBadge) {
                    LegendaryProfile.GOLD -> PrimalIcons.PrimalBadgeGold
                    LegendaryProfile.AQUA -> PrimalIcons.PrimalBadgeAqua
                    LegendaryProfile.SILVER -> PrimalIcons.PrimalBadgeSilver
                    LegendaryProfile.PURPLE -> PrimalIcons.PrimalBadgePurple
                    LegendaryProfile.PURPLE_HAZE -> PrimalIcons.PrimalBadgePurpleHaze
                    LegendaryProfile.TEAL -> PrimalIcons.PrimalBadgeTeal
                    LegendaryProfile.BROWN -> PrimalIcons.PrimalBadgeBrown
                    LegendaryProfile.BLUE -> PrimalIcons.PrimalBadgeBlue
                    LegendaryProfile.SUN_FIRE -> PrimalIcons.PrimalBadgeSunFire
                    LegendaryProfile.NO_CUSTOMIZATION -> throw IllegalStateException()
                }
                Icon(
                    imageVector = badgeVector,
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            } else {
                val surfaceColor = AppTheme.colorScheme.surface
                Image(
                    modifier = Modifier
                        .size(internetIdentifierBadgeSize)
                        .drawBehind {
                            drawCircle(
                                color = if (internetIdentifier.isPrimalIdentifier()) Color.White else surfaceColor,
                                radius = size.minDimension / 4.0f,
                            )
                        },
                    imageVector = PrimalIcons.Verified,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        color = if (internetIdentifier.isPrimalIdentifier()) {
                            AppTheme.colorScheme.tertiary
                        } else {
                            AppTheme.extraColorScheme.onSurfaceVariantAlt2
                        },
                    ),
                )
            }
        },
    )

    Text(
        modifier = modifier,
        text = titleText,
        fontSize = fontSize,
        textAlign = TextAlign.Start,
        overflow = overflow,
        inlineContent = inlineContent,
        maxLines = maxLines,
        style = style,
    )
}

@Preview
@Composable
fun PreviewNostrUserTextWithPrimalBadge() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            NostrUserText(
                displayName = "Nostr Adamus",
                internetIdentifier = "adam@primal.net",
                annotatedStringSuffixBuilder = {
                    append("• 42 y. ago")
                },
            )
        }
    }
}

@Preview
@Composable
fun PreviewNostrUserTextWithRandomBadge() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            NostrUserText(
                displayName = "Nostr Adamus",
                internetIdentifier = "adam@nostr.com",
                annotatedStringSuffixBuilder = {
                    append("• 42 y. ago")
                },
            )
        }
    }
}

@Preview
@Composable
fun PreviewNostrUserTextWithoutBadge() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            NostrUserText(
                displayName = "Nostr Adamus",
                internetIdentifier = null,
                annotatedStringSuffixBuilder = {
                    append(" • 42 y. ago")
                },
            )
        }
    }
}

@Preview
@Composable
fun PreviewNostrUserTextWithCustomBadge() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            NostrUserText(
                displayName = "Nostr Adamus",
                customBadge = LegendaryProfile.GOLD,
                internetIdentifier = "legend@primal.net",
                annotatedStringSuffixBuilder = {
                    append(" • 42 y. ago")
                },
            )
        }
    }
}
