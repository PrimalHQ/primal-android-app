package net.primal.android.premium.legend.become.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.premium.legend.become.BecomeLegendBottomBarButton
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@ExperimentalMaterial3Api
@Composable
fun BecomeLegendIntroStage(
    modifier: Modifier,
    onClose: () -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_become_legend_primal_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = true,
            )
        },
        bottomBar = {
            BecomeLegendBottomBarButton(
                text = stringResource(R.string.premium_become_legend_button_start),
                onClick = onNext,
            )
        },
    ) { paddingValues ->
        IntroContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun IntroContent(modifier: Modifier = Modifier) {
    val isDarkTheme = LocalPrimalTheme.current.isDarkTheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(56.dp))

        Text(
            text = stringResource(R.string.premium_become_legend_intro_subtitle),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyMedium,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimalLegendPerk(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.legend_perk_unlimited_badge),
            title = stringResource(R.string.premium_become_legend_intro_perk_unlimited_premium_title),
            description = stringResource(R.string.premium_become_legend_intro_perk_unlimited_premium_description),
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimalLegendPerk(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.legend_perk_more_storage_1),
            title = stringResource(R.string.premium_become_legend_intro_perk_more_storage_title),
            description = stringResource(R.string.premium_become_legend_intro_perk_more_storage_description),
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimalLegendPerk(
            modifier = Modifier.fillMaxWidth(),
            painter = if (isDarkTheme) {
                painterResource(R.drawable.legend_perk_profile_dark)
            } else {
                painterResource(R.drawable.legend_perk_profile)
            },
            title = stringResource(R.string.premium_become_legend_intro_perk_custom_profile_title),
            description = stringResource(R.string.premium_become_legend_intro_perk_custom_profile_description),
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.premium_become_legend_intro_gratitude_title),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyMedium,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.premium_become_legend_intro_gratitude_description),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
    }
}

@Composable
private fun PrimalLegendPerk(
    painter: Painter,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            modifier = Modifier,
            painter = painter,
            contentDescription = null,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.bodyMedium,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onSurface,
            )

            Text(
                text = description,
                style = AppTheme.typography.bodyMedium,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewIntro() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        BecomeLegendIntroStage(
            modifier = Modifier.fillMaxSize(),
            onClose = {},
            onNext = {},
        )
    }
}
