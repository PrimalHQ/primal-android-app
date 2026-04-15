package net.primal.android.user.domain

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ContentAppearance(
    val noteBodyFontSize: TextUnit,
    val noteBodyLineHeight: TextUnit,
    val noteUsernameSize: TextUnit,
    val replyAvatarSize: Dp,
    val noteAvatarSize: Dp,
    val articleTextFontSize: TextUnit,
    val articleTextLineHeight: TextUnit,
) {
    Small(
        noteBodyFontSize = 15.sp,
        noteBodyLineHeight = 22.sp,
        noteUsernameSize = 16.sp,
        replyAvatarSize = 32.dp,
        noteAvatarSize = 40.dp,
        articleTextFontSize = 15.sp,
        articleTextLineHeight = 26.sp,
    ),
    Default(
        noteBodyFontSize = 16.sp,
        noteBodyLineHeight = 22.sp,
        noteUsernameSize = 16.sp,
        replyAvatarSize = 34.dp,
        noteAvatarSize = 42.dp,
        articleTextFontSize = 16.sp,
        articleTextLineHeight = 28.sp,
    ),
    Large(
        noteBodyFontSize = 19.sp,
        noteBodyLineHeight = 25.sp,
        noteUsernameSize = 18.sp,
        replyAvatarSize = 36.dp,
        noteAvatarSize = 44.dp,
        articleTextFontSize = 18.sp,
        articleTextLineHeight = 30.sp,
    ),
    ExtraLarge(
        noteBodyFontSize = 21.sp,
        noteBodyLineHeight = 27.sp,
        noteUsernameSize = 18.sp,
        replyAvatarSize = 38.dp,
        noteAvatarSize = 46.dp,
        articleTextFontSize = 20.sp,
        articleTextLineHeight = 32.sp,
    ),
}
