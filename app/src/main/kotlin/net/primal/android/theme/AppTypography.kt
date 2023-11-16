package net.primal.android.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.primal.android.R

val NacelleFontFamily = FontFamily(
    Font(resId = R.font.nacelle_thin, weight = FontWeight.Thin, style = FontStyle.Normal),
    Font(resId = R.font.nacelle_thin_italic, weight = FontWeight.Thin, style = FontStyle.Italic),
    Font(
        resId = R.font.nacelle_ultra_light,
        weight = FontWeight.ExtraLight,
        style = FontStyle.Normal,
    ),
    Font(
        resId = R.font.nacelle_ultra_light_italic,
        weight = FontWeight.ExtraLight,
        style = FontStyle.Italic,
    ),
    Font(resId = R.font.nacelle_light, weight = FontWeight.Light, style = FontStyle.Normal),
    Font(resId = R.font.nacelle_light_italic, weight = FontWeight.Light, style = FontStyle.Italic),
    Font(resId = R.font.nacelle_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(resId = R.font.nacelle_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(resId = R.font.nacelle_semi_bold, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(
        resId = R.font.nacelle_semi_bold_italic,
        weight = FontWeight.SemiBold,
        style = FontStyle.Italic,
    ),
    Font(resId = R.font.nacelle_bold, weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(resId = R.font.nacelle_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic),
    Font(resId = R.font.nacelle_heavy, weight = FontWeight.ExtraBold, style = FontStyle.Normal),
    Font(
        resId = R.font.nacelle_heavy_italic,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Italic,
    ),
    Font(resId = R.font.nacelle_black, weight = FontWeight.Black, style = FontStyle.Normal),
    Font(resId = R.font.nacelle_black_italic, weight = FontWeight.Black, style = FontStyle.Italic),
)

val PrimalTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    displayLarge = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = NacelleFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
)
