package net.primal.android.user.domain

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ContentAppearance(
    val noteBodyFontSize: TextUnit,
    val noteBodyLineHeight: TextUnit,
    val noteUsernameSize: TextUnit,
    val noteAvatarSize: Dp,
    val articleTextFontSize: TextUnit,
    val articleTextLineHeight: TextUnit,
) {
    Small(
        noteBodyFontSize = 14.sp,
        noteBodyLineHeight = 17.sp,
        noteUsernameSize = 14.sp,
        noteAvatarSize = 28.dp,
        articleTextFontSize = 15.sp,
        articleTextLineHeight = 26.sp,
    ),
    Default(
        noteBodyFontSize = 15.sp,
        noteBodyLineHeight = 18.sp,
        noteUsernameSize = 14.sp,
        noteAvatarSize = 30.dp,
        articleTextFontSize = 16.sp,
        articleTextLineHeight = 28.sp,
    ),
    Large(
        noteBodyFontSize = 17.sp,
        noteBodyLineHeight = 20.sp,
        noteUsernameSize = 16.sp,
        noteAvatarSize = 32.dp,
        articleTextFontSize = 18.sp,
        articleTextLineHeight = 30.sp,
    ),
    ExtraLarge(
        noteBodyFontSize = 18.sp,
        noteBodyLineHeight = 22.sp,
        noteUsernameSize = 16.sp,
        noteAvatarSize = 34.dp,
        articleTextFontSize = 20.sp,
        articleTextLineHeight = 32.sp,
    ),
}
