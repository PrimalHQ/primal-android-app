package net.primal.android.auth.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.primal.android.theme.AppTheme

@Composable
fun onboardingTextHintTypography() =
    AppTheme.typography.bodyMedium.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
    )
