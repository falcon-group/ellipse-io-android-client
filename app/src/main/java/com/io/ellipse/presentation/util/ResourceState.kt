package com.io.ellipse.presentation.util

const val DEFAULT_SUCCESS_CODE = -1

private const val DEFAULT_VALUE = 0
private const val DEFAULT_MAX_VALUE = 100

sealed class ResourceState<T>

data class Progress(
    val isIntermediate: Boolean = true,
    val value: Int = DEFAULT_VALUE,
    val max: Int = DEFAULT_MAX_VALUE
) : ResourceState<Nothing>()

data class Failure(
    val message: String? = null,
    val error: Throwable? = null
) : ResourceState<Nothing>()

data class Success<T>(
    val code: Int = DEFAULT_SUCCESS_CODE,
    val data: T? = null
): ResourceState<T>()
