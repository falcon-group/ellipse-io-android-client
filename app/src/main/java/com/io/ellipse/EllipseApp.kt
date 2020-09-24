package com.io.ellipse

import android.app.Application
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EllipseApp : MultiDexApplication()