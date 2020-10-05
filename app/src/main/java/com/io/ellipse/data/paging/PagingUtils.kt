package com.io.ellipse.data.paging

import androidx.paging.PagingConfig

internal const val INITIAL_PAGE = 1
internal const val PAGE_SIZE = 20
internal const val PREFETCH_DISTANCE = 3
internal const val ENABLE_PLACEHOLDERS = false
internal const val INITIAL_LOAD_SIZE = PAGE_SIZE

val CONFIG: PagingConfig get() = PagingConfig(
    pageSize = PAGE_SIZE,
    prefetchDistance = PREFETCH_DISTANCE,
    enablePlaceholders = ENABLE_PLACEHOLDERS,
    initialLoadSize = INITIAL_LOAD_SIZE
)