package net.primal.android.stream.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.primal.android.LocalPrimalTheme

val ZapMessageBorderColor = Color(0xFFE47C00)

val ZapMessageBackgroundColor = Color(0xFFE47C00).copy(alpha = 0.2f)

val BottomSheetBackgroundPrimaryColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFF121212)
    } else {
        Color(0xFFF5F5F5)
    }

val BottomSheetBackgroundSecondaryColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFF222222)
    } else {
        Color(0xFFE5E5E5)
    }

val BottomSheetDividerColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFF333333)
    } else {
        Color(0xFFD5D5D5)
    }

val ZapMessageProfileHandleColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFFFFA02F)
    } else {
        Color(0xFFE47C00)
    }

val ReportButtonHandleColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFF333333)
    } else {
        Color(0xFFD5D5D5)
    }

val ActionButtonHandleColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFF333333)
    } else {
        Color(0xFFD5D5D5)
    }

val ChatBackgroundHandleColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color.Black
    } else {
        Color.White
    }
