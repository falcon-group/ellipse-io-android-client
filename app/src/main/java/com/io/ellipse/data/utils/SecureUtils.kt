package com.io.ellipse.data.utils

import java.util.*

private val RANDOM = Random()

fun random(size: Int): ByteArray = ByteArray(size).also { RANDOM.nextBytes(it) }