package net.primal.android.premium.support

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportPrimalScreen(viewModel: SupportPrimalViewModel, callbacks: SupportPrimalContract.ScreenCallbacks) {
    val state = viewModel.state.collectAsState()
    SupportPrimalScreen(
        state = state.value,
        callbacks = callbacks,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun SupportPrimalScreen(
    state: SupportPrimalContract.UiState,
    callbacks: SupportPrimalContract.ScreenCallbacks,
) {
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_support_primal_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = callbacks.onClose,
                showDivider = false,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.premium_support_primal_description),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                ),
            )

            Spacer(modifier = Modifier.height(20.dp))

            SupportCard(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(R.drawable.support_primal_give_five_stars),
                title = stringResource(R.string.premium_support_primal_review_call_title),
                description = stringResource(R.string.premium_support_primal_review_call_description),
                buttonText = stringResource(R.string.premium_support_primal_review_call_button_text),
                onClick = { openGooglePlayAppDetails(context = context) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (!state.hasMembership) {
                SupportCard(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(R.drawable.support_primal_buy_subscription),
                    title = stringResource(R.string.premium_support_primal_buy_subscription_title),
                    description = stringResource(R.string.premium_support_primal_buy_subscription_description),
                    buttonText = stringResource(R.string.premium_support_primal_buy_subscription_button_text),
                    onClick = { if (state.primalName != null) callbacks.onExtendSubscription(state.primalName) },
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            if (!state.isPrimalLegend) {
                SupportCard(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(R.drawable.support_primal_legend),
                    painterVerticalPadding = 0.dp,
                    title = stringResource(R.string.premium_support_primal_become_a_legend_title),
                    description = stringResource(R.string.premium_support_primal_become_a_legend_description),
                    buttonText = stringResource(R.string.premium_support_primal_become_a_legend_button_text),
                    onClick = callbacks.onBecomeLegend,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SupportCard(
    modifier: Modifier,
    painter: Painter,
    painterVerticalPadding: Dp = 8.dp,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                shape = AppTheme.shapes.large,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Image(
            modifier = Modifier.padding(vertical = painterVerticalPadding),
            painter = painter,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = AppTheme.typography.bodyLarge,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
        )

        TextButton(
            onClick = onClick,
        ) {
            Text(
                text = buttonText,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.secondary,
                fontSize = 16.sp,
            )
        }
    }
}

private fun openGooglePlayAppDetails(context: Context) {
    val packageName = context.packageName
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName&reviewId=0"),
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
