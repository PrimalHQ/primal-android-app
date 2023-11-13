package net.primal.android.theme.domain

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PostAppearance(
    val bodyFontSize: TextUnit,
    val lineHeight: TextUnit,
    val usernameFontSize: TextUnit,
    val avatarSize: Dp,
) {
    StandardSmall(
        bodyFontSize = 14.sp,
        lineHeight = 17.sp,
        usernameFontSize = 14.sp,
        avatarSize = 40.dp,
    ),
    StandardDefault(
        bodyFontSize = 15.sp,
        lineHeight = 17.sp,
        usernameFontSize = 15.sp,
        avatarSize = 42.dp,
    ),
    StandardLarge(
        bodyFontSize = 17.sp,
        lineHeight = 19.sp,
        usernameFontSize = 17.sp,
        avatarSize = 48.dp,
    ),
    StandardExtraLarge(
        bodyFontSize = 19.sp,
        lineHeight = 21.sp,
        usernameFontSize = 19.sp,
        avatarSize = 50.dp,
    ),
    FullWidthSmall(
        bodyFontSize = 14.sp,
        lineHeight = 17.sp,
        usernameFontSize = 14.sp,
        avatarSize = 28.dp,
    ),
    FullWidthDefault(
        bodyFontSize = 15.sp,
        lineHeight = 18.sp,
        usernameFontSize = 14.sp,
        avatarSize = 30.dp,
    ),
    FullWidthLarge(
        bodyFontSize = 17.sp,
        lineHeight = 20.sp,
        usernameFontSize = 16.sp,
        avatarSize = 32.dp,
    ),
    FullWidthExtraLarge(
        bodyFontSize = 18.sp,
        lineHeight = 22.sp,
        usernameFontSize = 16.sp,
        avatarSize = 34.dp,
    ),
}
