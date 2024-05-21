package net.primal.android.user.domain

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NoteAppearance(
    val bodyFontSize: TextUnit,
    val bodyLineHeight: TextUnit,
    val usernameSize: TextUnit,
    val avatarSize: Dp,
) {
    Small(
        bodyFontSize = 14.sp,
        bodyLineHeight = 17.sp,
        usernameSize = 14.sp,
        avatarSize = 40.dp,
    ),
    Default(
        bodyFontSize = 15.sp,
        bodyLineHeight = 18.sp,
        usernameSize = 14.sp,
        avatarSize = 42.dp,
    ),
    Large(
        bodyFontSize = 17.sp,
        bodyLineHeight = 20.sp,
        usernameSize = 16.sp,
        avatarSize = 48.dp,
    ),
    ExtraLarge(
        bodyFontSize = 18.sp,
        bodyLineHeight = 22.sp,
        usernameSize = 16.sp,
        avatarSize = 50.dp,
    ),
}
