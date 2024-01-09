@file:Suppress("KotlinConstantConditions", "unused")

package net.primal.android.core.utils

import net.primal.android.BuildConfig

private const val FLAVOR_PLAY = "google"
private const val FLAVOR_AOSP = "aosp"

fun isGoogleBuild() = BuildConfig.FLAVOR == FLAVOR_PLAY

fun isAospBuild() = BuildConfig.FLAVOR == FLAVOR_AOSP
