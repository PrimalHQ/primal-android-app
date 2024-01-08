@file:Suppress("KotlinConstantConditions", "unused")

package net.primal.android.core.utils

import net.primal.android.BuildConfig

private const val FLAVOR_PLAY = "play"
private const val FLAVOR_AOSP = "aosp"

fun isPlayBuild() = BuildConfig.FLAVOR == FLAVOR_PLAY

fun isAospBuild() = BuildConfig.FLAVOR == FLAVOR_AOSP
