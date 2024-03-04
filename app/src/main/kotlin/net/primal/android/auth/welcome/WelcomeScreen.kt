package net.primal.android.auth.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import net.primal.android.R
import net.primal.android.core.compose.ApplySystemBarColors
import net.primal.android.core.compose.ToSAndPrivacyPolicyText
import net.primal.android.core.compose.UiDensityMode
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.detectUiDensityModeFromMaxHeight
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun WelcomeScreen(onSignInClick: () -> Unit, onCreateAccountClick: () -> Unit) {
    ApplySystemBarColors(
        statusBarColor = Color.Transparent,
        navigationBarColor = Color.Transparent,
    )
    BoxWithConstraints {
        val uiMode = this.maxHeight.detectUiDensityModeFromMaxHeight()

        Image(
            modifier = Modifier.fillMaxSize(),
            imageVector = ImageVector.vectorResource(id = R.drawable.onboarding_bg_full),
            contentScale = ContentScale.FillHeight,
            alignment = OnboardingAlignment(fullXOffset = 0.0f),
            contentDescription = null,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier
                        .widthIn(240.dp, MAX_COMPONENT_WIDTH.dp)
                        .heightIn(
                            0.dp,
                            this@BoxWithConstraints.maxHeight * when (uiMode) {
                                UiDensityMode.Normal, UiDensityMode.Comfortable -> ONE_HALF
                                else -> TWO_FIFTHS
                            },
                        )
                        .fillMaxWidth(),
                    painter = painterResource(id = R.drawable.welcome),
                    contentDescription = null,
                    alignment = Alignment.BottomCenter,
                )

                Image(
                    modifier = Modifier.width(200.dp),
                    painter = painterResource(id = R.drawable.primal_logo),
                    contentDescription = null,
                )

                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "The Social Bitcoin Wallet",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                    ),
                )

                Spacer(
                    modifier = Modifier.height(
                        when (uiMode) {
                            UiDensityMode.Normal -> 24.dp
                            UiDensityMode.Comfortable -> 20.dp
                            else -> 16.dp
                        },
                    ),
                )

                WelcomeButton(
                    text = stringResource(id = R.string.welcome_sign_in_button_title),
                    onClick = onSignInClick,
                )

                Spacer(modifier = Modifier.height(8.dp))

                WelcomeButton(
                    text = stringResource(id = R.string.welcome_create_account_button_title),
                    onClick = onCreateAccountClick,
                )

                ToSAndPrivacyPolicyText(
                    modifier = Modifier
                        .widthIn(0.dp, MAX_COMPONENT_WIDTH.dp)
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    color = Color.White,
                    fontSize = 16.sp,
                    linksColor = Color.White,
                    tosPrefix = stringResource(id = R.string.welcome_tos_prefix),
                )
            }
        }
    }
}

class OnboardingAlignment(var fullXOffset: Float = 0.0f) : Alignment {
    override fun align(
        size: IntSize,
        space: IntSize,
        layoutDirection: LayoutDirection,
    ): IntOffset {
        val shiftedX = (fullXOffset * (size.width - space.width)) * -1f
        val centerY = (space.height - size.height).toFloat()
        return IntOffset(shiftedX.roundToInt(), centerY.roundToInt())
    }
}

@Composable
private fun WelcomeButton(text: String, onClick: () -> Unit) {
    PrimalFilledButton(
        modifier = Modifier
            .widthIn(240.dp, MAX_COMPONENT_WIDTH.dp)
            .fillMaxWidth(),
        containerColor = Color.Black,
        contentColor = Color.White,
        onClick = onClick,
    ) {
        Text(text = text)
    }
}

private const val MAX_COMPONENT_WIDTH = 320
private const val ONE_HALF = 0.5f
private const val TWO_FIFTHS = 0.4f

@Preview
@Composable
fun PreviewWelcomeScreen() {
    PrimalTheme(
        primalTheme = PrimalTheme.Sunset,
    ) {
        WelcomeScreen(
            onSignInClick = {},
            onCreateAccountClick = {},
        )
    }
}
