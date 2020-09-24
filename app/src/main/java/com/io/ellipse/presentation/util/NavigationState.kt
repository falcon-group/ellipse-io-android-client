package com.io.ellipse.presentation.util

import android.app.Activity
import android.os.Bundle
import androidx.core.os.bundleOf

sealed class NavigationState

data class BackState(
    val code: Int = Activity.RESULT_CANCELED,
    val arguments: MutableMap<String, Any?> = mutableMapOf()
): NavigationState()

abstract class NextScreenState: NavigationState()

fun BackState.toBundle() : Bundle {
    return bundleOf(*arguments.map { it.key to it.value }.toTypedArray())
}